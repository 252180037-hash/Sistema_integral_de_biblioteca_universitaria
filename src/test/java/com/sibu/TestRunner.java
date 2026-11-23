package com.sibu;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  EJECUTANDO BATERIA DE PRUEBAS DE CALIDAD SIBU  ");
        System.out.println("=================================================");

        Class<?> testClass = SibuServicioTest.class;
        int exitosos = 0;
        int fallidos = 0;

        try {
            // Ejecutar métodos @BeforeAll
            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(BeforeAll.class) && Modifier.isStatic(m.getModifiers())) {
                    m.setAccessible(true);
                    m.invoke(null);
                }
            }

            Object instance = testClass.getDeclaredConstructor().newInstance();

            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Test.class)) {
                    DisplayName dn = m.getAnnotation(DisplayName.class);
                    String name = dn != null ? dn.value() : m.getName();
                    System.out.print("[TEST] " + name + " ... ");
                    try {
                        m.setAccessible(true);
                        m.invoke(instance);
                        System.out.println("PASO OK");
                        exitosos++;
                    } catch (Exception ex) {
                        System.out.println("FALLO!");
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        cause.printStackTrace(System.out);
                        fallidos++;
                    }
                }
            }

            // Ejecutar métodos @AfterAll
            for (Method m : testClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(AfterAll.class) && Modifier.isStatic(m.getModifiers())) {
                    m.setAccessible(true);
                    m.invoke(null);
                }
            }

        } catch (Exception e) {
            System.err.println("Error durante la inicializacion de pruebas: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("=================================================");
        System.out.println("RESULTADOS: " + exitosos + " PASARON, " + fallidos + " FALLARON");
        System.out.println("=================================================");

        if (fallidos > 0) {
            System.exit(1);
        }
    }
}
