package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private List<Libro> libros;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {

        // ELIMINAR LUEGO >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        String json = consumoAPI.obtenerDatosLibros(URL_BASE);
        System.out.println(json);

        var data = conversor.obtenerDatos(json, Datos.class);
        System.out.println(data);
        // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

        var opcion = -1;

        while (opcion != 0) {
            var menu = """
                    
                    **************************************************
                    *                 ~ LITERALURA ~                 *
                    **************************************************
                    *                 MENU PRINCIPAL                 *
                    **************************************************
                    1 - Buscar Libro por Titulo
                    2 - Listar Libros Registrados
                    3 - Listar Autores Registrados
                    4 - Listar Autores vivos en un determinado año
                    5 - Listar Libros por Idioma
                    
                    0 - Salir
                    """;
            System.out.println(menu);
            System.out.print("Opcion: ");
            String opcionMenu = sc.nextLine();
            try {
                opcion = Integer.parseInt(opcionMenu);
            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese una Opción válida [numero entero].");
                continue;
            }

//            opcion = obtenerOpcion();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
//                case 3:
//                    listarAutoresRegistrados();
//                    break;
//                case 4:
//                    listarAutoresVivosEnAño();
//                    break;
//                case 5:
//                    listarLibrosPorIdioma();
//                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }

        }
    }

    private void buscarLibroPorTitulo() {
        DatosLibros datosLibros = getDatosLibros();
        if (datosLibros != null) {
            // Verificar si el libro ya existe en la base de datos
            Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibros.titulo());

            if (libroExistente.isPresent()) {
                System.out.println("El libro ya está registrado en el sistema.");
                return; // Salir del metodo si el libro ya existe
            }

            // Si el libro no existe, procedemos con los autores
            List<Autor> autores = new ArrayList<>();

            // Para cada autor en DatosLibros
            for (DatosAutor datosAutor : datosLibros.autor()) {
                // Buscar si el autor ya existe en la base de datos
                Optional<Autor> autorExistente = autorRepository.findByNombre(datosAutor.nombre());

                // Si el autor existe, se usa o si no, crea uno nuevo
                Autor autor = autorExistente.orElseGet(() -> new Autor(datosAutor));

                // Agrega el autor a la lista de autores del libro
                autores.add(autor);
            }

            // Crear el libro con los datos de datosLibros y añadir los autores
            Libro libro = new Libro(datosLibros);
            libro.setAutores(autores);

            // Guardar el libro junto con sus autores en la base de datos
            libroRepository.save(libro);
            System.out.println("Libro Guardado Exitosamente.");
        } else {
            System.out.println("Libro no Encontrado");
        }
    }

    private DatosLibros getDatosLibros() {
        System.out.print("Ingresa el nombre del libro que deseas buscar: ");
        var nombreLibro = sc.nextLine();
        // Buscar libro en la API
        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibro.replace(" ", "+")); // me trae un json
        System.out.println(json); // SOLO VER LUEGO>BORRAR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        // Convierto json a un objeto Java
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        System.out.println(datosBusqueda);

        Optional<DatosLibros> libroBuscado = datosBusqueda.listaResultados().stream()
                .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst();

        if (libroBuscado.isPresent()) {
            System.out.println(libroBuscado.get());
            return libroBuscado.get();
        } else {
            return null;
        }
    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAll();
        libros.forEach(libro -> System.out.println(libro));
    }
}
