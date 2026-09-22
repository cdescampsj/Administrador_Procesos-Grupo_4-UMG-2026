/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package administrador.de.procesos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public List<ProcesoInfo> obtenerProcesosActivos() {
        List<ProcesoInfo> listaProcesos = new ArrayList<>();
        registrarLog("Actualizando procesos y uso de RAM del sistema...");

        try {
            String comandoPowerShell = "Get-Process | Where-Object {$_.Path -ne $null} | " +
                    "Select-Object ProcessName, Path, @{Name='RAM';Expression={[math]::Round($_.WorkingSet64 / 1MB, 2)}} | " +
                    "ConvertTo-Csv -NoTypeInformation";

            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", comandoPowerShell);
            Process p = pb.start();

            BufferedReader lector = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String linea;

            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("\"ProcessName\"")) {
                    String[] partes = linea.split("\",\"");
                    if (partes.length >= 3) {
                        String nombre = partes[0].replace("\"", "") + ".exe";
                        String rutaCompleta = partes[1].replace("\"", "");

                        double ram = 0;
                        try {
                            ram = Double.parseDouble(partes[2].replace("\"", "").replace(",", "."));
                        } catch (NumberFormatException ignored) {}

                        mapaRutasProcesos.put(nombre.toLowerCase(), rutaCompleta);
                        listaProcesos.add(new ProcesoInfo(nombre, ram));
                    }
                }
            }
            registrarLog("Lista actualizada (" + listaProcesos.size() + " procesos cargados).");
        } catch (Exception ex) {
            registrarLog("Error al listar procesos: " + ex.getMessage());
        }

        return listaProcesos;
    }
}
