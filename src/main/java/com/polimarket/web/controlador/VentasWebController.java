package com.polimarket.web.controlador;

import com.polimarket.web.cliente.ApiClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequestMapping("/ventas")
public class VentasWebController {

    private final ApiClientService api;

    public VentasWebController(ApiClientService api) {
        this.api = api;
    }

    // 1. PETICIÓN GET PARA ABRIR LA VISTA DE VENTAS Y CLIENTES
    @GetMapping
    public String vistaVentas(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || modulos == null || !modulos.contains("MOD_VENTAS")) {
            return "redirect:/";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("clientes", api.get("/ventas/clientes", List.class));
        model.addAttribute("ventas", api.get("/ventas/todas", List.class));

        // Filtrar en tiempo real productos con stockDisponible > 0
        List<Map<String, Object>> todosProductos = api.get("/bodega/productos", List.class);
        List<Map<String, Object>> disponibles = new ArrayList<>();
        if (todosProductos != null) {
            for (Map<String, Object> p : todosProductos) {
                Object stock = p.get("stockDisponible");
                if (stock != null && Integer.parseInt(stock.toString()) > 0) {
                    disponibles.add(p);
                }
            }
        }
        model.addAttribute("disponibles", disponibles);
        return "ventas";
    }

    // 2. PETICIÓN POST PARA GUARDAR O ACTUALIZAR CLIENTE
    @PostMapping("/clientes/guardar")
    public String guardarCliente(@RequestParam String documento, @RequestParam String nombre,
                                 @RequestParam String direccionEntrega, @RequestParam String telefono,
                                 HttpSession session, RedirectAttributes flash) {
        api.post("/ventas/clientes", Map.of(
            "documento", documento,
            "nombre", nombre,
            "direccionEntrega", direccionEntrega,
            "telefono", telefono
        ), (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Cliente '" + nombre + "' guardado correctamente");
        return "redirect:/ventas";
    }

    // 3. PETICIÓN POST PARA ELIMINAR CLIENTE
    @PostMapping("/clientes/eliminar")
    public String eliminarCliente(@RequestParam String documento, HttpSession session, RedirectAttributes flash) {
        api.deleteConUsuario("/ventas/clientes/" + documento, (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Cliente eliminado del directorio");
        return "redirect:/ventas";
    }

    // 4. PETICIÓN POST PARA REGISTRAR VENTA
    @PostMapping("/registrar")
    public String registrarVenta(@RequestParam String documentoCliente, @RequestParam String codigoProducto,
                                 @RequestParam int cantidad, HttpSession session, RedirectAttributes flash) {
        Map<String, Object> body = Map.of(
            "documentoCliente", documentoCliente,
            "codigoProducto", codigoProducto,
            "cantidad", cantidad
        );
        Map res = api.post("/ventas/registrar", body, (String) session.getAttribute("usuario"));
        if (res != null && Boolean.TRUE.equals(res.get("exito"))) {
            flash.addFlashAttribute("exito", "Venta registrada con éxito");
        } else {
            String msg = res != null && res.get("mensaje") != null ? res.get("mensaje").toString() : "Stock insuficiente";
            flash.addFlashAttribute("error", msg);
        }
        return "redirect:/ventas";
    }
}
