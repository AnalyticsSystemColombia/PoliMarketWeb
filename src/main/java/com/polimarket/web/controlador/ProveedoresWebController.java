package com.polimarket.web.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.polimarket.web.cliente.ApiClientService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/proveedores")
public class ProveedoresWebController {

    private final ApiClientService api;

    public ProveedoresWebController(ApiClientService api) {
        this.api = api;
    }

    @GetMapping
    public String vistaProveedores(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || !modulos.contains("MOD_PROVEEDORES")) {
            return "redirect:/";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("proveedores", api.get("/proveedores/todos", List.class));
        return "proveedores";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(@RequestParam String nit, @RequestParam String nombre,
                                   @RequestParam String contacto, @RequestParam String telefono,
                                   HttpSession session, RedirectAttributes flash) {
        api.post("/proveedores/crear", Map.of(
            "nit", nit,
            "nombre", nombre,
            "contacto", contacto,
            "telefono", telefono
        ), (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Proveedor '" + nombre + "' guardado correctamente");
        return "redirect:/proveedores";
    }

    @PostMapping("/eliminar")
    public String eliminarProveedor(@RequestParam String nit, HttpSession session, RedirectAttributes flash) {
        api.deleteConUsuario("/proveedores/eliminar/" + nit, (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Proveedor eliminado del directorio");
        return "redirect:/proveedores";
    }
}
