package xyz.dowenliu.core.generic;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * <p>create at 2019/11/4</p>
 *
 * @author liufl
 */
public class GenericReflectionTest {
    public static void main(String[] args) {
        // read class name from command line args or user input
        String name;
        if (args.length > 0) {
            name = args[0];
        } else {
            try (Scanner in = new Scanner(System.in)) {
                System.out.println("Enter class name:");
                name = in.nextLine();
            }
        }

        try {
            // print generic info for class and public methods
            Class<?> cl = Class.forName(name);
            printClass(cl);
            for (Method m : cl.getDeclaredMethods()) {
                printMethod(m);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void printClass(Class<?> cl) {
        if (cl == null) {
            return;
        }
        System.out.print(cl);
        printTypes(cl.getTypeParameters(), "<", ", ", ">", true);
        Type sc = cl.getGenericSuperclass();
        if (sc != null) {
            System.out.print(" extends ");
            printType(sc, false);
        }
        printTypes(cl.getGenericInterfaces(), " implements ", ", ", "", false);
        System.out.println();
    }

    private static void printMethod(Method m) {
        String name = m.getName();
        System.out.print(Modifier.toString(m.getModifiers()));
        System.out.print(" ");
        printTypes(m.getTypeParameters(), "<", ", ", "> ", true);

        printType(m.getGenericReturnType(), false);
        System.out.print(" ");
        System.out.print(name);
        System.out.print("(");
        printTypes(m.getGenericParameterTypes(), "", ", ", "", false);
        System.out.println(")");
    }

    private static void printTypes(Type[] types, String pre, String sep, String suf, boolean isDefinition) {
        if (types == null || types.length == 0) {
            return;
        }
        if (" extends ".equals(pre) && Arrays.equals(types, new Type[]{Object.class})) return;
        System.out.print(pre);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                System.out.print(sep);
            }
            printType(types[i], isDefinition);
        }
        System.out.print(suf);
    }

    private static void printType(Type type, boolean isDefinition) {
        if (type == null) {
            return;
        }
        if (type instanceof Class) {
            Class<?> t = (Class<?>) type;
            System.out.print(t.getName());
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> t = (TypeVariable<?>) type;
            System.out.print(t.getName());
            if (isDefinition) {
                printTypes(t.getBounds(), " extends ", " & ", "", false);
            }
        } else if (type instanceof WildcardType) {
            WildcardType t = (WildcardType) type;
            System.out.print("?");
            printTypes(t.getUpperBounds(), " extends ", " & ", "", false);
            printTypes(t.getLowerBounds(), " super ", " & ", "", false);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType t = (ParameterizedType) type;
            Type owner = t.getOwnerType();
            if (owner != null) {
                printType(owner, false);
                System.out.print(".");
            }
            printType(t.getRawType(), false);
            printTypes(t.getActualTypeArguments(), "<", ", ", ">", false);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType t = (GenericArrayType) type;
            System.out.print("");
            printType(t.getGenericComponentType(), isDefinition);
            System.out.print("[]");
        }
    }
}
