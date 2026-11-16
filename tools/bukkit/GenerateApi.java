import java.io.IOException;
import java.lang.classfile.Attributes;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassSignature;
import java.lang.classfile.CodeElement;
import java.lang.classfile.FieldModel;
import java.lang.classfile.Instruction;
import java.lang.classfile.MethodModel;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.classfile.instruction.FieldInstruction;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.classfile.instruction.LoadInstruction;
import java.lang.classfile.instruction.NewObjectInstruction;
import java.lang.classfile.instruction.ReturnInstruction;
import java.lang.classfile.instruction.StackInstruction;
import java.lang.classfile.instruction.TypeCheckInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;

// Writes org.bukkit enums, interfaces and events that tube-bukkit-api lacks, from the shape of a reference API jar.
// Interfaces only get the methods that scanned plugins call. Event methods with logic beyond field access throw UnsupportedOperationException.
// usage: java -cp <tube-bukkit-api classes and dependencies> GenerateApi.java <reference api jar> <tube-bukkit-api source dir> <scan.json> <internal class name...>
public class GenerateApi {

    static ZipFile jar;
    static Path source;
    static final Set<String> original = new java.util.HashSet<>();
    static final Map<String, Boolean> generatableCache = new HashMap<>();

    static boolean generatable(String internal) {
        Boolean cached = generatableCache.get(internal);
        if (cached != null) {
            return cached;
        }
        boolean result;
        try {
            String top = internal.contains("$") ? internal.substring(0, internal.indexOf('$')) : internal;
            if (original.contains(top)) {
                result = !internal.contains("$") || existsNested(internal);
            } else {
                ClassModel m = model(internal);
                if (m == null) {
                    result = false;
                } else if (internal.contains("$")) {
                    result = (m.flags().has(AccessFlag.ENUM) || m.flags().has(AccessFlag.INTERFACE)) && !m.flags().has(AccessFlag.ANNOTATION) && generatable(top);
                } else {
                    result = !m.flags().has(AccessFlag.ANNOTATION) && (m.flags().has(AccessFlag.ENUM) || m.flags().has(AccessFlag.INTERFACE) || isEvent(m));
                }
            }
        } catch (IOException e) {
            result = false;
        }
        generatableCache.put(internal, result);
        return result;
    }
    static final Map<String, Set<String>> measured = new HashMap<>();
    static final Deque<String> queue = new ArrayDeque<>();
    static final Set<String> done = new LinkedHashSet<>();
    static final Set<String> manual = new TreeSet<>();
    static final Set<String> written = new TreeSet<>();

    public static void main(String[] args) throws Exception {
        jar = new ZipFile(args[0]);
        source = Path.of(args[1]);
        try (var files = Files.walk(source)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                String rel = source.relativize(p).toString().replace('\\', '/');
                original.add(rel.substring(0, rel.length() - 5));
            });
        }
        var scan = com.google.gson.JsonParser.parseString(Files.readString(Path.of(args[2]))).getAsJsonObject();
        for (String ref : scan.getAsJsonObject("members").keySet()) {
            int hash = ref.indexOf('#');
            if (hash > 0) {
                measured.computeIfAbsent(ref.substring(0, hash), k -> new LinkedHashSet<>()).add(ref.substring(hash + 1));
            }
        }
        for (int i = 3; i < args.length; i++) {
            queue.add(args[i]);
        }
        while (!queue.isEmpty()) {
            String name = queue.poll();
            String top = name.contains("$") ? name.substring(0, name.indexOf('$')) : name;
            if (!done.add(top) || exists(top)) {
                if (name.contains("$") && exists(top) && !existsNested(name)) {
                    manual.add(name + " (nested in existing type)");
                }
                continue;
            }
            generate(top);
        }
        written.forEach(w -> System.out.println("WROTE " + w));
        manual.forEach(m -> System.out.println("MANUAL " + m));
    }

    static boolean exists(String internal) {
        return Files.exists(source.resolve(internal + ".java"));
    }

    static boolean existsNested(String internal) {
        String top = internal.substring(0, internal.indexOf('$'));
        try {
            String text = Files.readString(source.resolve(top + ".java"));
            String simple = internal.substring(internal.lastIndexOf('$') + 1);
            return text.matches("(?s).*\\b(class|interface|enum|record)\\s+" + simple + "\\b.*");
        } catch (IOException e) {
            return false;
        }
    }

    static ClassModel model(String internal) throws IOException {
        var entry = jar.getEntry(internal + ".class");
        if (entry == null) {
            return null;
        }
        return ClassFile.of().parse(jar.getInputStream(entry).readAllBytes());
    }

    static boolean isBukkit(String internal) {
        return internal.startsWith("org/bukkit/");
    }

    static boolean available(String internal) {
        if (isBukkit(internal)) {
            return true;
        }
        try {
            Class.forName(internal.replace('/', '.'), false, GenerateApi.class.getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    static boolean isEvent(ClassModel model) throws IOException {
        ClassModel current = model;
        while (current != null && current.superclass().isPresent()) {
            String parent = current.superclass().get().asInternalName();
            if (parent.equals("org/bukkit/event/Event")) {
                return true;
            }
            current = model(parent);
        }
        return false;
    }

    static void generate(String internal) throws IOException {
        ClassModel model = model(internal);
        if (model == null) {
            manual.add(internal + " (not in reference jar)");
            return;
        }
        boolean isEnum = model.flags().has(AccessFlag.ENUM);
        boolean isInterface = model.flags().has(AccessFlag.INTERFACE);
        boolean isAnnotation = model.flags().has(AccessFlag.ANNOTATION);
        if (isAnnotation) {
            manual.add(internal + " (annotation)");
            return;
        }
        StringBuilder out = new StringBuilder();
        String pkg = internal.substring(0, internal.lastIndexOf('/')).replace('/', '.');
        out.append("package ").append(pkg).append(";\n\n");
        Body body = new Body(internal);
        if (isEnum) {
            body.enumType(model, "");
        } else if (isInterface) {
            body.interfaceType(model, "");
        } else if (isEvent(model)) {
            body.eventType(model, "");
        } else {
            manual.add(internal + " (plain class)");
            return;
        }
        out.append(body.text);
        Path file = source.resolve(internal + ".java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, out.toString());
        written.add(internal);
    }

    static final class Body {
        final String self;
        final StringBuilder text = new StringBuilder();

        Body(String self) {
            this.self = self;
        }

        String type(ClassDesc desc) {
            if (desc.isPrimitive()) {
                return desc.displayName();
            }
            if (desc.isArray()) {
                return type(desc.componentType()) + "[]";
            }
            String internal = desc.descriptorString().substring(1, desc.descriptorString().length() - 1);
            require(internal);
            return internal.replace('/', '.').replace('$', '.');
        }

        void require(String internal) {
            if (isBukkit(internal) && !internal.equals(self) && !internal.startsWith(self + "$")) {
                queue.add(internal);
            }
        }

        boolean usable(ClassDesc desc) {
            if (desc.isPrimitive()) {
                return true;
            }
            if (desc.isArray()) {
                return usable(desc.componentType());
            }
            String internal = desc.descriptorString().substring(1, desc.descriptorString().length() - 1);
            if (isBukkit(internal)) {
                return !internal.startsWith("org/bukkit/craftbukkit") && generatable(internal);
            }
            return available(internal) && !internal.startsWith("net/md_5");
        }

        boolean usable(MethodTypeDesc desc) {
            if (!usable(desc.returnType())) {
                return false;
            }
            for (ClassDesc p : desc.parameterList()) {
                if (!usable(p)) {
                    return false;
                }
            }
            return true;
        }

        String generic(Signature sig) {
            return switch (sig) {
                case Signature.BaseTypeSig base -> ClassDesc.ofDescriptor(String.valueOf(base.baseType())).displayName();
                case Signature.ArrayTypeSig array -> generic(array.componentSignature()) + "[]";
                case Signature.TypeVarSig var -> var.identifier();
                case Signature.ClassTypeSig cls -> {
                    String name = type(cls.classDesc());
                    if (cls.outerType().isPresent()) {
                        name = generic(cls.outerType().get()) + "." + cls.className().substring(cls.className().lastIndexOf('$') + 1);
                    }
                    if (cls.typeArgs().isEmpty()) {
                        yield name;
                    }
                    List<String> args = new ArrayList<>();
                    for (Signature.TypeArg arg : cls.typeArgs()) {
                        args.add(switch (arg) {
                            case Signature.TypeArg.Unbounded _ -> "?";
                            case Signature.TypeArg.Bounded b -> switch (b.wildcardIndicator()) {
                                case NONE -> generic(b.boundType());
                                case EXTENDS -> "? extends " + generic(b.boundType());
                                case SUPER -> "? super " + generic(b.boundType());
                            };
                        });
                    }
                    yield name + "<" + String.join(", ", args) + ">";
                }
            };
        }

        String typeParams(List<Signature.TypeParam> params) {
            if (params.isEmpty()) {
                return "";
            }
            List<String> out = new ArrayList<>();
            for (Signature.TypeParam p : params) {
                String bound = p.classBound().map(this::generic).orElse(null);
                List<String> bounds = new ArrayList<>();
                if (bound != null && !bound.equals("java.lang.Object")) {
                    bounds.add(bound);
                }
                p.interfaceBounds().forEach(i -> bounds.add(generic(i)));
                out.add(p.identifier() + (bounds.isEmpty() ? "" : " extends " + String.join(" & ", bounds)));
            }
            return "<" + String.join(", ", out) + "> ";
        }

        record Sig(String typeParams, String returnType, List<String> params, List<String> exceptions) {
        }

        Sig signature(MethodModel method) {
            var attr = method.findAttribute(Attributes.signature());
            MethodTypeDesc desc = method.methodTypeSymbol();
            if (attr.isPresent()) {
                MethodSignature sig = attr.get().asMethodSignature();
                List<String> params = new ArrayList<>();
                sig.arguments().forEach(a -> params.add(generic(a)));
                if (params.size() == desc.parameterCount()) {
                    return new Sig(typeParams(sig.typeParameters()), generic(sig.result()), params, List.of());
                }
            }
            List<String> params = new ArrayList<>();
            desc.parameterList().forEach(p -> params.add(type(p)));
            return new Sig("", type(desc.returnType()), params, List.of());
        }

        String header(ClassModel model, String kind, String indent) {
            String simple = simpleName(model.thisClass().asInternalName());
            StringBuilder h = new StringBuilder(indent);
            h.append(model.flags().has(AccessFlag.PUBLIC) || indent.isEmpty() ? "public " : "");
            if (indent.length() > 0 && !kind.equals("interface") && !kind.equals("enum")) {
                h.append("static ");
            }
            if (kind.equals("class") && model.flags().has(AccessFlag.ABSTRACT)) {
                h.append("abstract ");
            }
            h.append(kind).append(' ').append(simple);
            var sigAttr = model.findAttribute(Attributes.signature());
            ClassSignature classSig = sigAttr.map(s -> s.asClassSignature()).orElse(null);
            if (classSig != null) {
                h.append(typeParams(classSig.typeParameters()).stripTrailing());
            }
            if (kind.equals("class") && model.superclass().isPresent() && !model.superclass().get().asInternalName().equals("java/lang/Object")) {
                h.append(" extends ").append(classSig != null ? generic(classSig.superclassSignature()) : type(model.superclass().get().asSymbol()));
            }
            List<String> interfaces = new ArrayList<>();
            if (classSig != null) {
                for (var i : classSig.superinterfaceSignatures()) {
                    if (usable(i.classDesc())) {
                        interfaces.add(generic(i));
                    }
                }
            } else {
                model.interfaces().forEach(i -> {
                    if (usable(i.asSymbol())) {
                        interfaces.add(type(i.asSymbol()));
                    }
                });
            }
            if (!interfaces.isEmpty() && !kind.equals("enum")) {
                h.append(kind.equals("interface") ? " extends " : " implements ").append(String.join(", ", interfaces));
            }
            return h.append(" {\n").toString();
        }

        String simpleName(String internal) {
            return internal.substring(Math.max(internal.lastIndexOf('/'), internal.lastIndexOf('$')) + 1);
        }

        void nestedEnums(ClassModel model, String indent) throws IOException {
            var inner = model.findAttribute(Attributes.innerClasses());
            if (inner.isEmpty()) {
                return;
            }
            for (var info : inner.get().classes()) {
                String name = info.innerClass().asInternalName();
                if (info.outerClass().isPresent() && info.outerClass().get().asInternalName().equals(model.thisClass().asInternalName())) {
                    ClassModel nested = model(name);
                    if (nested != null && nested.flags().has(AccessFlag.ENUM)) {
                        text.append("\n");
                        enumType(nested, indent + "    ");
                    } else if (nested != null && nested.flags().has(AccessFlag.INTERFACE) && !nested.flags().has(AccessFlag.ANNOTATION)) {
                        text.append("\n");
                        interfaceType(nested, indent + "    ");
                    }
                }
            }
        }

        void enumType(ClassModel model, String indent) throws IOException {
            text.append(header(model, "enum", indent));
            List<String> constants = new ArrayList<>();
            for (FieldModel field : model.fields()) {
                if (field.flags().has(AccessFlag.ENUM)) {
                    constants.add(indent + "    " + field.fieldName().stringValue());
                }
            }
            text.append(String.join(",\n", constants)).append(constants.isEmpty() ? "" : "\n");
            text.append(indent).append("}\n");
        }

        void interfaceType(ClassModel model, String indent) throws IOException {
            text.append(header(model, "interface", indent));
            Set<String> wanted = measured.getOrDefault(model.thisClass().asInternalName(), Set.of());
            for (MethodModel method : model.methods()) {
                String key = method.methodName().stringValue() + method.methodType().stringValue();
                if (!wanted.contains(key) || method.flags().has(AccessFlag.STATIC) || !usable(method.methodTypeSymbol())) {
                    continue;
                }
                Sig sig = signature(method);
                text.append("\n").append(indent).append("    ").append(sig.typeParams()).append(sig.returnType()).append(' ')
                        .append(method.methodName().stringValue()).append('(').append(params(sig.params())).append(");\n");
            }
            nestedEnums(model, indent);
            text.append(indent).append("}\n");
        }

        String params(List<String> types) {
            List<String> out = new ArrayList<>();
            for (int i = 0; i < types.size(); i++) {
                out.add(types.get(i) + " p" + i);
            }
            return String.join(", ", out);
        }

        void eventType(ClassModel model, String indent) throws IOException {
            String internal = model.thisClass().asInternalName();
            text.append(header(model, "class", indent));
            String in = indent + "    ";
            boolean hasHandlers = false;
            for (FieldModel field : model.fields()) {
                String name = field.fieldName().stringValue();
                if (field.flags().has(AccessFlag.STATIC)) {
                    if (field.fieldTypeSymbol().descriptorString().equals("Lorg/bukkit/event/HandlerList;")) {
                        hasHandlers = true;
                    }
                    continue;
                }
                if (!usable(field.fieldTypeSymbol())) {
                    continue;
                }
                var sig = field.findAttribute(Attributes.signature());
                String type = sig.isPresent() ? generic(sig.get().asTypeSignature()) : type(field.fieldTypeSymbol());
                String access = field.flags().has(AccessFlag.PROTECTED) ? "protected " : "private ";
                text.append(in).append(access).append(type).append(' ').append(name).append(";\n");
            }
            if (hasHandlers) {
                text.append(in).append("private static final org.bukkit.event.HandlerList HANDLERS = new org.bukkit.event.HandlerList();\n");
            }
            String parent = model.superclass().map(c -> c.asInternalName()).orElse("");
            for (MethodModel method : model.methods()) {
                String name = method.methodName().stringValue();
                if (method.flags().has(AccessFlag.SYNTHETIC) || method.flags().has(AccessFlag.BRIDGE) || method.flags().has(AccessFlag.PRIVATE)
                        || name.equals("<clinit>") || !usable(method.methodTypeSymbol())) {
                    continue;
                }
                String access = method.flags().has(AccessFlag.PUBLIC) ? "public " : method.flags().has(AccessFlag.PROTECTED) ? "protected " : "";
                Sig sig = signature(method);
                text.append("\n");
                if (name.equals("<init>")) {
                    text.append(in).append(access).append(simpleName(internal)).append('(').append(params(sig.params())).append(") {\n");
                    text.append(constructorBody(method, internal, parent, in + "    "));
                    text.append(in).append("}\n");
                    continue;
                }
                boolean isStatic = method.flags().has(AccessFlag.STATIC);
                boolean isAbstract = method.flags().has(AccessFlag.ABSTRACT);
                if (name.equals("getHandlerList") && isStatic) {
                    text.append(in).append("public static org.bukkit.event.HandlerList getHandlerList() {\n").append(in).append("    return HANDLERS;\n").append(in).append("}\n");
                    continue;
                }
                if (name.equals("getHandlers") && !isStatic) {
                    text.append(in).append("@Override\n").append(in).append("public org.bukkit.event.HandlerList getHandlers() {\n").append(in).append("    return HANDLERS;\n").append(in).append("}\n");
                    continue;
                }
                text.append(in).append(access).append(isStatic ? "static " : "").append(isAbstract ? "abstract " : "")
                        .append(sig.typeParams()).append(sig.returnType()).append(' ').append(name).append('(').append(params(sig.params())).append(')');
                if (isAbstract) {
                    text.append(";\n");
                    continue;
                }
                text.append(" {\n").append(methodBody(method, sig, in + "    ")).append(in).append("}\n");
            }
            nestedEnums(model, indent);
            text.append(indent).append("}\n");
        }

        List<Instruction> instructions(MethodModel method) {
            List<Instruction> list = new ArrayList<>();
            method.code().ifPresent(code -> {
                for (CodeElement e : code) {
                    if (e instanceof Instruction i) {
                        list.add(i);
                    }
                }
            });
            return list;
        }

        String value(Instruction i, MethodModel method) {
            if (i instanceof LoadInstruction load) {
                int slot = load.slot();
                int index = method.flags().has(AccessFlag.STATIC) ? 0 : 1;
                int param = 0;
                for (ClassDesc p : method.methodTypeSymbol().parameterList()) {
                    if (index == slot) {
                        return "p" + param;
                    }
                    index += (p.descriptorString().equals("J") || p.descriptorString().equals("D")) ? 2 : 1;
                    param++;
                }
                return slot == 0 ? "this" : null;
            }
            if (i instanceof ConstantInstruction constant) {
                Object v = constant.constantValue();
                if (v == null || constant.opcode().name().equals("ACONST_NULL")) {
                    return "null";
                }
                if (v instanceof String s) {
                    return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
                }
                if (v instanceof Integer n && constant.typeKind().name().equals("INT")) {
                    return n.toString();
                }
                if (v instanceof Long n) {
                    return n + "L";
                }
                if (v instanceof Float n) {
                    return n + "F";
                }
                if (v instanceof Double n) {
                    return n + "D";
                }
                return null;
            }
            if (i instanceof FieldInstruction field && field.opcode().name().equals("GETSTATIC")) {
                return type(field.owner().asSymbol()) + "." + field.name().stringValue();
            }
            return null;
        }

        String constructorBody(MethodModel method, String internal, String parent, String in) {
            List<Instruction> code = instructions(method);
            StringBuilder b = new StringBuilder();
            List<String> stack = new ArrayList<>();
            boolean superDone = false;
            for (int k = 0; k < code.size(); k++) {
                Instruction i = code.get(k);
                if (i instanceof InvokeInstruction invoke && invoke.name().stringValue().equals("<init>") && !superDone) {
                    String owner = invoke.owner().asInternalName();
                    int argc = invoke.typeSymbol().parameterCount();
                    List<String> args = stack.subList(Math.max(0, stack.size() - argc), stack.size());
                    if (args.contains(null) || args.size() != argc) {
                        b.append(in).append(owner.equals(internal) ? "this" : "super").append("(").append(defaults(invoke.typeSymbol())).append(");\n");
                    } else {
                        List<String> typed = new ArrayList<>();
                        List<ClassDesc> types = invoke.typeSymbol().parameterList();
                        for (int a = 0; a < args.size(); a++) {
                            typed.add(cast(types.get(a), booleanValue(types.get(a), args.get(a))));
                        }
                        b.append(in).append(owner.equals(internal) ? "this" : "super").append("(").append(String.join(", ", typed)).append(");\n");
                    }
                    stack.clear();
                    superDone = true;
                    if (owner.equals(internal)) {
                        return b.toString();
                    }
                    continue;
                }
                if (i instanceof FieldInstruction field && field.opcode().name().equals("PUTFIELD") && superDone) {
                    if (stack.size() >= 2 && "this".equals(stack.get(stack.size() - 2)) && stack.getLast() != null
                            && field.owner().asInternalName().equals(internal) && usable(field.typeSymbol())) {
                        b.append(in).append("this.").append(field.name().stringValue()).append(" = ").append(booleanValue(field.typeSymbol(), stack.getLast())).append(";\n");
                    }
                    stack.clear();
                    continue;
                }
                if (i instanceof NewObjectInstruction alloc && k + 2 < code.size() && code.get(k + 1) instanceof StackInstruction
                        && code.get(k + 2) instanceof InvokeInstruction init && init.name().stringValue().equals("<init>") && init.typeSymbol().parameterCount() == 0) {
                    stack.add("new " + type(alloc.className().asSymbol()) + "()");
                    k += 2;
                    continue;
                }
                if (i instanceof TypeCheckInstruction check && check.opcode().name().equals("CHECKCAST")
                        || i instanceof java.lang.classfile.instruction.ConvertInstruction) {
                    continue;
                }
                String v = value(i, method);
                if (v != null || i instanceof LoadInstruction || i instanceof ConstantInstruction) {
                    stack.add(v);
                } else if (!(i instanceof ReturnInstruction)) {
                    stack.add(null);
                }
            }
            return b.toString();
        }

        String booleanValue(ClassDesc type, String value) {
            if (type.descriptorString().equals("Z")) {
                return value.equals("0") ? "false" : value.equals("1") ? "true" : value;
            }
            return value;
        }

        String defaults(MethodTypeDesc desc) {
            List<String> out = new ArrayList<>();
            for (ClassDesc p : desc.parameterList()) {
                out.add(cast(p, p.isPrimitive() ? (p.descriptorString().equals("Z") ? "false" : "0") : "null"));
            }
            return String.join(", ", out);
        }

        String cast(ClassDesc type, String value) {
            if (value.equals("null") && !type.isPrimitive()) {
                return "(" + type(type) + ") null";
            }
            if (type.isPrimitive() && !type.descriptorString().equals("Z") && value.matches("-?\\d+")) {
                return "(" + type.displayName() + ") " + value;
            }
            return value;
        }

        String methodBody(MethodModel method, Sig sig, String in) {
            List<Instruction> code = instructions(method);
            String name = method.methodName().stringValue();
            if (code.size() <= 4 && !code.isEmpty() && code.getLast() instanceof ReturnInstruction) {
                if (code.size() >= 3 && code.get(0) instanceof LoadInstruction l && l.slot() == 0 && code.get(1) instanceof FieldInstruction f && f.opcode().name().equals("GETFIELD")) {
                    if (code.size() == 3) {
                        return in + "return " + f.name().stringValue() + ";\n";
                    }
                    if (code.get(2) instanceof TypeCheckInstruction) {
                        return in + "return (" + sig.returnType() + ") " + f.name().stringValue() + ";\n";
                    }
                }
                if (code.size() == 4 && code.get(0) instanceof LoadInstruction l0 && l0.slot() == 0 && code.get(2) instanceof FieldInstruction f && f.opcode().name().equals("PUTFIELD")) {
                    String v = value(code.get(1), method);
                    if (v != null) {
                        return in + "this." + f.name().stringValue() + " = " + booleanValue(f.typeSymbol(), v) + ";\n";
                    }
                }
                if (code.size() == 2 && value(code.get(0), method) != null && !sig.returnType().equals("void")) {
                    return in + "return " + value(code.get(0), method) + ";\n";
                }
                if (code.size() == 1 && sig.returnType().equals("void")) {
                    return "";
                }
            }
            if (name.equals("isCancelled") || name.equals("setCancelled")) {
                for (Instruction i : code) {
                    if (i instanceof FieldInstruction f) {
                        return name.equals("isCancelled") ? in + "return " + f.name().stringValue() + ";\n" : in + "this." + f.name().stringValue() + " = p0;\n";
                    }
                }
            }
            return in + "throw new UnsupportedOperationException(\"" + simpleName(self) + "#" + name + " is not supported by Tube yet\");\n";
        }
    }
}
