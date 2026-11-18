package com.polimarket.web.controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.polimarket.web.cliente.ApiClientService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final ApiClientService api;

    public AuthController(ApiClientService api) {
        this.api = api;
    }

    @GetMapping("/")
    public String inicio(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "login";
        }
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (modulos == null || modulos.isEmpty()) return "sin-acceso";
        if (modulos.contains("MOD_SEGURIDAD")) return "redirect:/rrhh";
        if (modulos.contains("MOD_PROVEEDORES")) return "redirect:/proveedores";
        if (modulos.contains("MOD_BODEGA")) return "redirect:/bodega";
        if (modulos.contains("MOD_VENTAS")) return "redirect:/ventas";
        if (modulos.contains("MOD_ENTREGAS")) return "redirect:/entregas";
        return "sin-acceso";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Map res = api.post("/usuarios/login", Map.of("username", username, "password", password), null);
        if (res != null && Boolean.TRUE.equals(res.get("autenticado"))) {
            session.setAttribute("usuario", res.get("username"));
            session.setAttribute("nombre", res.get("nombreCompleto"));

            List<Map<String, Object>> perms = (List<Map<String, Object>>) res.get("permisos");
            List<String> codigos = new ArrayList<>();
            Map<String, Map<String, Object>> permisosMap = new HashMap<>();

            if (perms != null) {
                for (Map<String, Object> p : perms) {
                    String mod = (String) p.get("codigoModulo");
                    codigos.add(mod);
                    permisosMap.put(mod, p);
                }
            }
            session.setAttribute("modulos", codigos);
            session.setAttribute("permisosMap", permisosMap);
            return "redirect:/";
        }
        model.addAttribute("error", "Credenciales incorrectas o servidor API apagado");
        return "login";
    }

    @GetMapping("/salir")
    public String salir(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
