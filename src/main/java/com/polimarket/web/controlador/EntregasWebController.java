package com.polimarket.web.controlador;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.polimarket.web.cliente.ApiClientService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/entregas")
public class EntregasWebController {

    private final ApiClientService api;

    public EntregasWebController(ApiClientService api) {
        this.api = api;
    }

    @GetMapping
    public String vistaEntregas(HttpSession session, Model model) {
        List<String> modulos = (List<String>) session.getAttribute("modulos");
        if (session.getAttribute("usuario") == null || !modulos.contains("MOD_ENTREGAS")) {
            return "redirect:/";
        }
        model.addAttribute("nombreUsuario", session.getAttribute("nombre"));
        model.addAttribute("modulos", modulos);
        model.addAttribute("permisosMap", session.getAttribute("permisosMap"));
        model.addAttribute("pendientes", api.get("/entregas/pendientes", List.class));
        model.addAttribute("entregas", api.get("/entregas/todas", List.class));
        return "entregas";
    }

    @PostMapping("/programar")
    public String programarEntrega(@RequestParam Long idVenta, @RequestParam String fechaProgramada,
                                   @RequestParam String transportador, @RequestParam String guiaDespacho,
                                   HttpSession session, RedirectAttributes flash) {
        Map<String, Object> body = Map.of(
            "idVenta", idVenta,
            "fechaProgramada", fechaProgramada,
            "transportador", transportador,
            "guiaDespacho", guiaDespacho
        );
        api.post("/entregas/programar", body, (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Envío programado para la Venta #" + idVenta);
        return "redirect:/entregas";
    }

    @PostMapping("/despachar/{id}")
    public String despachar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        api.post("/entregas/despachar/" + id, Map.of(), (String) session.getAttribute("usuario"));
        flash.addFlashAttribute("exito", "Salida física confirmada (Stock físico descontado en Bodega)");
        return "redirect:/entregas";
    }
}
