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
@RequestMapping("/rrhh")
public class RrhhWebController {

    private final ApiClientService api;

    public RrhhWebController(ApiClientService api) {
        this.api = api;
    }

    @GetMapping
    public String vistaRrhh(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || !modulos.contains("MOD_SEGURIDAD")) {
            return "redirect:/";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("usuariosLista", api.get("/usuarios/todos", List.class));
        model.addAttribute("modulosLista", api.get("/usuarios/modulos", List.class));
        model.addAttribute("permisosLista", api.get("/usuarios/permisos/todos", List.class));
        return "rrhh";
    }

    @PostMapping("/usuarios")
    public String guardarUsuario(@RequestParam String username, @RequestParam String password,
                                 @RequestParam String nombreCompleto, @RequestParam String cargo,
                                 RedirectAttributes flash) {
        api.post("/usuarios/crear", Map.of(
            "username", username,
            "password", password,
            "nombreCompleto", nombreCompleto,
            "cargo", cargo
        ), null);
        flash.addFlashAttribute("exito", "Empleado '" + nombreCompleto + "' guardado correctamente");
        return "redirect:/rrhh";
    }

    @PostMapping("/permisos")
    public String asignarPermiso(@RequestParam String username, @RequestParam String codigoModulo,
                                 @RequestParam(defaultValue = "false") boolean puedeLeer,
                                 @RequestParam(defaultValue = "false") boolean puedeCrear,
                                 @RequestParam(defaultValue = "false") boolean puedeEditar,
                                 @RequestParam(defaultValue = "false") boolean puedeEliminar,
                                 RedirectAttributes flash) {
        api.post("/usuarios/permisos/asignar", Map.of(
            "username", username,
            "codigoModulo", codigoModulo,
            "puedeLeer", puedeLeer,
            "puedeCrear", puedeCrear,
            "puedeEditar", puedeEditar,
            "puedeEliminar", puedeEliminar
        ), null);
        flash.addFlashAttribute("exito", "Permisos actualizados para '" + username + "'");
        return "redirect:/rrhh";
    }

    @PostMapping("/permisos/revocar")
    public String revocarPermiso(@RequestParam String username, @RequestParam String codigoModulo,
                                 RedirectAttributes flash) {
        api.delete("/usuarios/permisos/revocar/" + username + "/" + codigoModulo);
        flash.addFlashAttribute("exito", "Acceso revocado en " + codigoModulo);
        return "redirect:/rrhh";
    }

    @PostMapping("/modulos")
    public String crearModulo(@RequestParam String codigo, @RequestParam String nombre,
                              @RequestParam String descripcion, RedirectAttributes flash) {
        api.post("/usuarios/modulos/crear", Map.of(
            "codigo", codigo,
            "nombre", nombre,
            "descripcion", descripcion
        ), null);
        flash.addFlashAttribute("exito", "Módulo '" + nombre + "' registrado en el sistema");
        return "redirect:/rrhh";
    }
}
