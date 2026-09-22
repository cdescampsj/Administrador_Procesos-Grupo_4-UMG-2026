/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package administrador.de.procesos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 * @author Chris
 */
public class Funciones {
     public static class ProcesoInfo {
        private String nombre;
        private double consumoRAM;

        public ProcesoInfo(String nombre, double consumoRAM) {
            this.nombre = nombre;
            this.consumoRAM = consumoRAM;
        }

        public String getNombre() { return nombre; }
        public double getConsumoRAM() { return consumoRAM; }

        @Override
        public String toString() {
            if (consumoRAM > 0) {
                return String.format("%s (%.1f MB)", nombre, consumoRAM);
            }
            return nombre;
        }
    }

    private Map<String, String> mapaRutasProcesos = new HashMap<>();
    private Map<String, Integer> contadorUsoApps = new HashMap<>(); // Registro para "Más Usados"
    private Consumer<String> logCallback;

    public Funciones(Consumer<String> logCallback) {
        this.logCallback = logCallback;
    }

    private void registrarLog(String mensaje) {
        if (logCallback != null) {
            logCallback.accept(mensaje);
        }
    }

    public Map<String, Integer> getContadorUsoApps() {
        return contadorUsoApps;
    }
}
