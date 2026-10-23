package com.tiendaropa.tiendaropa.controller;

import com.tiendaropa.tiendaropa.model.Pedido;
import com.tiendaropa.tiendaropa.model.Producto;
import com.tiendaropa.tiendaropa.repository.PedidoRepository;
import com.tiendaropa.tiendaropa.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;

@Controller
public class ClienteController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("pedidos", pedidoRepository.findAll());
        if (!model.containsAttribute("nuevoProducto")) {
            model.addAttribute("nuevoProducto", new Producto());
        }
        return "index";
    }

    @PostMapping("/realizar-compra")
    public String realizarCompra(@RequestParam("productoId") Long productoId,
                                 @RequestParam("clienteNombre") String clienteNombre,
                                 @RequestParam("clienteDireccion") String clienteDireccion,
                                 @RequestParam("clienteTelefono") String clienteTelefono) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto != null && producto.getStock() > 0) {
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);

            Pedido pedido = new Pedido();
            pedido.setClienteNombre(clienteNombre);
            pedido.setClienteDireccion(clienteDireccion);
            pedido.setClienteTelefono(clienteTelefono);
            pedido.setTotal(producto.getPrecio());
            pedido.setVendedorId("ONLINE");
            pedido.setEstado("Completado");
            pedidoRepository.save(pedido);
        }
        return "redirect:/";
    }

    @PostMapping("/venta-vendedor")
    public String ventaVendedor(@RequestParam("idVendedor") String idVendedor,
                                @RequestParam("productoId") Long productoId,
                                @RequestParam("clienteNombre") String clienteNombre) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto != null && producto.getStock() > 0) {
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);

            Pedido pedido = new Pedido();
            pedido.setClienteNombre(clienteNombre);
            pedido.setClienteDireccion("Venta en tienda física");
            pedido.setClienteTelefono("N/A");
            pedido.setTotal(producto.getPrecio());
            pedido.setVendedorId(idVendedor);
            pedido.setEstado("Cobrado en caja");
            pedidoRepository.save(pedido);
        }
        return "redirect:/";
    }

    @PostMapping("/admin/guardar-producto")
    public String guardarProducto(@ModelAttribute("nuevoProducto") Producto producto,
                                  @RequestParam(value = "archivoImagen", required = false) MultipartFile archivoImagen) {
        try {
            if (archivoImagen != null && !archivoImagen.isEmpty()) {
                String nombreArchivo = System.currentTimeMillis() + "_" + archivoImagen.getOriginalFilename();
                Path ruta = Paths.get(UPLOAD_DIR + nombreArchivo);
                Files.createDirectories(ruta.getParent());
                Files.write(ruta, archivoImagen.getBytes());
                producto.setImagenNombre(nombreArchivo);
            } else if (producto.getId() != null) {
                Producto prodExistente = productoRepository.findById(producto.getId()).orElse(null);
                if (prodExistente != null) {
                    producto.setImagenNombre(prodExistente.getImagenNombre());
                }
            }
            productoRepository.save(producto);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping("/admin/eliminar/{id}")
    public String eliminarProducto(@PathVariable("id") Long id) {
        productoRepository.deleteById(id);
        return "redirect:/";
    }
}