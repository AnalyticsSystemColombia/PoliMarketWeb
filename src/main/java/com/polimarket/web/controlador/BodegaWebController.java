package com.polimarket.web.controlador;

import com.polimarket.web.cliente.ApiClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
@RequestMapping("/bodega")
public class BodegaWebController {

    private final ApiClientService api;

    public BodegaWebController(ApiClientService api) {
        this.api = api;
    }

    // VISTA 1: INVENTARIO MAESTRO (Productos ya dados de alta con stock > 0)
    @GetMapping({ "", "/productos" })
    public String vistaProductos(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || modulos == null || !modulos.contains("MOD_BODEGA")) {
            return "redirect:/";
        }
        List<Map<String, Object>> todos = api.get("/bodega/productos", List.class);
        List<Map<String, Object>> dadosDeAlta = new ArrayList<>();
        int pendientesCount = 0;

        if (todos != null) {
            for (Map<String, Object> p : todos) {
                int fisico = p.get("stockFisico") != null ? Integer.parseInt(p.get("stockFisico").toString()) : 0;
                if (fisico > 0) {
                    dadosDeAlta.add(p);
                } else {
                    pendientesCount++;
                }
            }
        }

        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("proveedores", api.get("/proveedores/todos", List.class));
        model.addAttribute("productos", dadosDeAlta);
        model.addAttribute("pendientesCount", pendientesCount);
        return "bodega-productos";
    }

    // GUARDAR O EDITAR REFERENCIA DE PRODUCTO (Nace en 0 -> pasa a Pendientes por Dar de Alta)
    @PostMapping("/productos")
    public String guardarProducto(@RequestParam String codigo, @RequestParam String marca,
                                  @RequestParam String categoria, @RequestParam String nombre,
                                  @RequestParam String caracteristicas, @RequestParam double precioVenta,
                                  @RequestParam int stockMinimo, @RequestParam String nitProveedorPrincipal,
                                  HttpSession session, RedirectAttributes flash) {
        api.post("/bodega/productos", Map.of(
            "codigo", codigo, "marca", marca, "categoria", categoria,
            "nombre", nombre, "caracteristicas", caracteristicas,
            "precioVenta", precioVenta, "stockMinimo", stockMinimo,
            "nitProveedorPrincipal", nitProveedorPrincipal
        ), (String) session.getAttribute("usuario"));

        if ("AUTO".equalsIgnoreCase(codigo)) {
            flash.addFlashAttribute("exito", "Referencia creada. Ve a 'Pendientes por Dar de Alta' para ingresar su stock.");
            return "redirect:/bodega/facturas";
        }
        flash.addFlashAttribute("exito", "Producto actualizado en el Inventario Maestro");
        return "redirect:/bodega/productos";
    }

    // VISTA 2: PRODUCTOS PENDIENTES POR DAR DE ALTA + HISTORIAL DE FACTURAS
    @GetMapping("/facturas")
    public String vistaAltasYFacturas(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || modulos == null || !modulos.contains("MOD_BODEGA")) {
            return "redirect:/";
        }
        List<Map<String, Object>> todos = api.get("/bodega/productos", List.class);
        List<Map<String, Object>> pendientesAlta = new ArrayList<>();

        if (todos != null) {
            for (Map<String, Object> p : todos) {
                int fisico = p.get("stockFisico") != null ? Integer.parseInt(p.get("stockFisico").toString()) : 0;
                if (fisico == 0) {
                    pendientesAlta.add(p);
                }
            }
        }

        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("proveedores", api.get("/proveedores/todos", List.class));
        model.addAttribute("pendientesAlta", pendientesAlta);
        model.addAttribute("todosProductos", todos);
        model.addAttribute("facturas", api.get("/bodega/facturas", List.class));
        return "bodega-facturas";
    }

    // PROCESAR ALTA AL INVENTARIO MAESTRO MEDIANTE FACTURA DE COMPRA
    @PostMapping("/facturas")
    public String darDeAltaProducto(@RequestParam String nitProveedor, @RequestParam String codigoProducto,
                                    @RequestParam int cantidadIngresada, @RequestParam double costoUnitario,
                                    HttpSession session, RedirectAttributes flash) {
        api.post("/bodega/facturas", Map.of(
            "numeroFactura", "AUTO",
            "nitProveedor", nitProveedor,
            "codigoProducto", codigoProducto,
            "cantidadIngresada", cantidadIngresada,
            "costoUnitario", costoUnitario
        ), (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Producto dado de alta e ingresado al Inventario Maestro (+ " + cantidadIngresada + " und.)");
        return "redirect:/bodega/productos";
    }
}
