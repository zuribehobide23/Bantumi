package es.upm.miw.bantumi.datos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PartidaGuardada {
    private final String id;
    private final String nombre;
    private final String fechaHora;
    private final String datosSerializados;

    public PartidaGuardada(String id, String nombre, String fechaHora, String datosSerializados) {
        this.id = id;
        this.nombre = nombre;
        this.fechaHora = fechaHora;
        this.datosSerializados = datosSerializados;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public String getDatosSerializados() {
        return datosSerializados;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("nombre", nombre);
        json.put("fechaHora", fechaHora);
        json.put("datosSerializados", datosSerializados);
        return json;
    }

    public static PartidaGuardada fromJsonObject(JSONObject json) throws JSONException {
        return new PartidaGuardada(
                json.getString("id"),
                json.getString("nombre"),
                json.getString("fechaHora"),
                json.getString("datosSerializados")
        );
    }

    public static String listToJson(List<PartidaGuardada> lista) {
        JSONArray array = new JSONArray();
        for (PartidaGuardada p : lista) {
            try {
                array.put(p.toJsonObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array.toString();
    }

    public static List<PartidaGuardada> jsonToList(String jsonStr) {
        List<PartidaGuardada> lista = new ArrayList<>();
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return lista;
        }
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                lista.add(fromJsonObject(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
