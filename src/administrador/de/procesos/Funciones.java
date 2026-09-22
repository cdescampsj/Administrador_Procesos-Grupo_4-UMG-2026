/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package administrador.de.procesos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
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
    
      public List<ProcesoInfo> filtrarYOrdenar(List<ProcesoInfo> listaOriginal, String textoBusqueda, int criterioOrden) {
        List<ProcesoInfo> listaTrabajo = new ArrayList<>(listaOriginal);

        // Criterios de orden
        switch (criterioOrden) {
            case 0: // Mayor consumo de RAM
                listaTrabajo.sort((p1, p2) -> Double.compare(p2.getConsumoRAM(), p1.getConsumoRAM()));
                break;
            case 1: // Alfabético (A-Z)
                listaTrabajo.sort(Comparator.comparing(p -> p.getNombre().toLowerCase()));
                break;
            case 2: // Más Usados / Frecuentes
                listaTrabajo.sort((p1, p2) -> {
                    int uso1 = contadorUsoApps.getOrDefault(p1.getNombre().toLowerCase(), 0);
                    int uso2 = contadorUsoApps.getOrDefault(p2.getNombre().toLowerCase(), 0);
                    return Integer.compare(uso2, uso1);
                });
                break;
        }

        // Filtro por búsqueda de texto
        if (textoBusqueda == null || textoBusqueda.trim().isEmpty()) {
            return listaTrabajo;
        }

        String busqueda = textoBusqueda.toLowerCase().trim();
        List<ProcesoInfo> filtrados = new ArrayList<>();
        for (ProcesoInfo p : listaTrabajo) {
            if (p.getNombre().toLowerCase().contains(busqueda)) {
                filtrados.add(p);
            }
        }
        return filtrados;
    }
    
    
}
//comentario 