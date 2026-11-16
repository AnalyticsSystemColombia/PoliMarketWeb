package com.polimarket.web.controlador;

import com.polimarket.web.cliente.ApiClientService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
public class PortalWebController {

    private final ApiClientService api;

    public PortalWebController(ApiClientService api) {
        this.api = api;
    }
    
    @PostMapping("/ventas/cliente/guardar")
    public String guardarCliente(@RequestParam String documento, @RequestParam String nombre,
                                 @RequestParam String direccionEntrega, @RequestParam String telefono,
                                 HttpSession session) {
        Map<String, String> body = Map.of(
            "documento", documento,
            "nombre", nombre,
            "direccionEntrega", direccionEntrega,
            "telefono", telefono
        );
        // Spring JPA save() crea si el documento es nuevo, o actualiza si ya existe
        api.post("/ventas/clientes", body, (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/ventas/cliente/eliminar/{documento}")
    public String eliminarCliente(@PathVariable String documento, HttpSession session) {
        api.deleteConUsuario("/ventas/clientes/" + documento, (String) session.getAttribute("usuario"));
        return "redirect:/";
    }


    @GetMapping("/")
    public String inicio(HttpSession session, Model model) {
        if (session.getAttribute("usuario") == null) {
            return "login";
        }
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        
        if (modulos.contains("MOD_SEGURIDAD")) {
            model.addAttribute("usuariosLista", api.get("/usuarios/todos", List.class));
            model.addAttribute("modulosLista", api.get("/usuarios/modulos", List.class));
        }

        if (modulos.contains("MOD_PROVEEDORES") || modulos.contains("MOD_BODEGA")) {
            model.addAttribute("proveedores", api.get("/proveedores/todos", List.class));
        }
        if (modulos.contains("MOD_BODEGA")) {
            model.addAttribute("productos", api.get("/bodega/productos", List.class));
        }
        if (modulos.contains("MOD_VENTAS")) {
            model.addAttribute("clientes", api.get("/ventas/clientes", List.class));
            model.addAttribute("disponibles", api.get("/ventas/catalogo-disponible", List.class));
            model.addAttribute("ventas", api.get("/ventas/todas", List.class));
        }
        if (modulos.contains("MOD_ENTREGAS")) {
            model.addAttribute("pendientes", api.get("/entregas/pendientes", List.class));
            model.addAttribute("entregas", api.get("/entregas/todas", List.class));
        }
        return "dashboard";
    }
    @PostMapping("/usuarios/crear")
    public String crearUsuario(@RequestParam String username, @RequestParam String password,
                                 @RequestParam String nombreCompleto, @RequestParam String cargo) {
        api.post("/usuarios/crear", Map.of("username", username, "password", password, "nombreCompleto", nombreCompleto, "cargo", cargo), null);
        return "redirect:/";
    }

    @PostMapping("/usuarios/permiso")
    public String asignarPermiso(@RequestParam String username, @RequestParam String codigoModulo) {
        api.post("/usuarios/permisos/asignar", Map.of("username", username, "codigoModulo", codigoModulo, "nivelAcceso", "TOTAL"), null);
        return "redirect:/";
    }

    @PostMapping("/usuarios/modulo")
    public String crearModulo(@RequestParam String codigo, @RequestParam String nombre, @RequestParam String descripcion) {
        api.post("/usuarios/modulos/crear", Map.of("codigo", codigo, "nombre", nombre, "descripcion", descripcion), null);
        return "redirect:/";
    }


    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Map res = api.post("/usuarios/login", Map.of("username", username, "password", password), null);
        if (res != null && Boolean.TRUE.equals(res.get("autenticado"))) {
            session.setAttribute("usuario", res.get("username"));
            session.setAttribute("nombre", res.get("nombreCompleto"));
            List<Map<String, String>> perms = (List<Map<String, String>>) res.get("permisos");
            session.setAttribute("modulos", perms.stream().map(p -> p.get("codigoModulo")).toList());
            return "redirect:/";
        }
        model.addAttribute("error", "Credenciales incorrectas o usuario inactivo");
        return "login";
    }

    @GetMapping("/salir")
    public String salir(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/proveedores/crear")
    public String crearProveedor(@RequestParam String nit, @RequestParam String nombre,
                                 @RequestParam String contacto, @RequestParam String telefono, HttpSession session) {
        api.post("/proveedores/crear", Map.of("nit", nit, "nombre", nombre, "contacto", contacto, "telefono", telefono), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/bodega/producto")
    public String crearProducto(@RequestParam String codigo, @RequestParam String nombre,
                                @RequestParam double precioVenta, @RequestParam int stockMinimo,
                                @RequestParam String nitProveedorPrincipal, HttpSession session) {
        api.post("/bodega/productos", Map.of("codigo", codigo, "nombre", nombre, "precioVenta", precioVenta, "stockMinimo", stockMinimo, "nitProveedorPrincipal", nitProveedorPrincipal), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/bodega/factura")
    public String ingresarFactura(@RequestParam String numeroFactura, @RequestParam String nitProveedor,
                                  @RequestParam String codigoProducto, @RequestParam int cantidadIngresada,
                                  @RequestParam double costoUnitario, HttpSession session) {
        api.post("/bodega/facturas", Map.of("numeroFactura", numeroFactura, "nitProveedor", nitProveedor, "codigoProducto", codigoProducto, "cantidadIngresada", cantidadIngresada, "costoUnitario", costoUnitario), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/ventas/registrar")
    public String registrarVenta(@RequestParam String documentoCliente, @RequestParam String codigoProducto,
                                 @RequestParam int cantidad, HttpSession session) {
        api.post("/ventas/registrar", Map.of("documentoCliente", documentoCliente, "codigoProducto", codigoProducto, "cantidad", cantidad), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/entregas/programar")
    public String programarEntrega(@RequestParam Long idVenta, @RequestParam String fechaProgramada,
                                   @RequestParam String transportador, @RequestParam String guiaDespacho, HttpSession session) {
        api.post("/entregas/programar", Map.of("idVenta", idVenta, "fechaProgramada", fechaProgramada, "transportador", transportador, "guiaDespacho", guiaDespacho), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }

    @PostMapping("/entregas/despachar/{id}")
    public String despachar(@PathVariable Long id, HttpSession session) {
        api.post("/entregas/despachar/" + id, Map.of(), (String) session.getAttribute("usuario"));
        return "redirect:/";
    }
}
