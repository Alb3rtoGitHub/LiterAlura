package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.dto.AutorDTO;
import com.aluracursos.literalura.dto.LibroDTO;
import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private List<Libro> libros;
    private List<Autor> autores;

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
                case 3:
                    listarAutoresRegistrados();
                    break;
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

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Para buscar todos los libros por titulo y registrar varios si existen
    private void buscarLibroPorTitulo() {
        System.out.print("Ingresa el nombre del libro que deseas buscar: ");
        var nombreLibro = sc.nextLine();
        Datos datosBusqueda = getDatosLibros(nombreLibro);

        if (datosBusqueda != null) {
            datosBusqueda.listaResultados().stream()
                    .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                    .forEach(datosLibros -> {
                        // Verificar si el libro ya existe en la base de datos
                        Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibros.titulo());

                        if (libroExistente.isPresent()) {
                            System.out.println("El libro "+ datosLibros.titulo().toUpperCase() + " ya esta registrado.");
                        } else {
                            // Registrar autores y libro si no existe
                            List<Autor> autores = new ArrayList<>();

                            for (DatosAutor datosAutor : datosLibros.autor()){
                                Optional<Autor> autorExistente = autorRepository.findByNombre(datosAutor.nombre());
                                Autor autor = autorExistente.orElseGet(() -> {
                                    Autor autorNuevo = new Autor();
                                    autorNuevo.setNombre(datosAutor.nombre());
                                    autorRepository.save(autorNuevo);
                                    return autorNuevo;
                                });
                                autores.add(autor);
                            }

                            Libro libro = new Libro(datosLibros);
                            libro.setAutores(autores);
                            libroRepository.save(libro);
                            System.out.println("Libro '" + datosLibros.titulo() + "' guardado exitosamente.");
                        }
                    });

        } else {
            System.out.println("No se encontraron libros con ese título.");
        }

    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


//    private void buscarLibroPorTitulo() {
//        DatosLibros datosLibros = getDatosLibros();
//        if (datosLibros != null) {
//            // Verificar si el libro ya existe en la base de datos
//            Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibros.titulo());
//
//            if (libroExistente.isPresent()) {
//                System.out.println("El libro ya está registrado en el sistema.");
//                return; // Salir del metodo si el libro ya existe
//            }
//
//            // Si el libro no existe, procedemos con los autores
//            List<Autor> autores = new ArrayList<>();
//
//            // Para cada autor en DatosLibros
//            for (DatosAutor datosAutor : datosLibros.autor()) {
//                // Buscar si el autor ya existe en la base de datos
//                Optional<Autor> autorExistente = autorRepository.findByNombre(datosAutor.nombre());
//
//                // Si el autor existe, se usa o si no, crea uno nuevo
//                Autor autor = autorExistente.orElseGet(() -> new Autor(datosAutor));
//
//                // Agrega el autor a la lista de autores del libro
//                autores.add(autor);
//            }
//
//            // Crear el libro con los datos de datosLibros y añadir los autores
//            Libro libro = new Libro(datosLibros);
//            libro.setAutores(autores);
//
//            // Guardar el libro junto con sus autores en la base de datos
//            libroRepository.save(libro);
//            System.out.println("Libro Guardado Exitosamente.");
//        } else {
//            System.out.println("Libro no Encontrado");
//        }
//    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //Para traer toda la lista y devolverla
    private Datos getDatosLibros(String nombreLibroBuscado) {

        // Buscar libro en la API
        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibroBuscado.replace(" ", "+"));

        // Convertir JSON a un objeto Java
        Datos datosBusqueda = conversor.obtenerDatos(json, Datos.class);

        return datosBusqueda;
    }
    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>




//    private DatosLibros getDatosLibros() {
//        System.out.print("Ingresa el nombre del libro que deseas buscar: ");
//        var nombreLibro = sc.nextLine();
//        // Buscar libro en la API
//        String json = consumoAPI.obtenerDatosLibros(URL_BASE + "?search=" + nombreLibro.replace(" ", "+")); // me trae un json
//        System.out.println(json); // SOLO VER LUEGO>BORRAR>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//        // Convierto json a un objeto Java
//        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
//        System.out.println(datosBusqueda);
//
//        Optional<DatosLibros> libroBuscado = datosBusqueda.listaResultados().stream()
//                .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
//                .findFirst();
//
//        if (libroBuscado.isPresent()) {
//            System.out.println(libroBuscado.get());
//            return libroBuscado.get();
//        } else {
//            return null;
//        }
//    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAllWithAutores();

        if (libros.isEmpty()) {
            System.out.printf("No hay libros registrados en el sistema.");
            return;
        }

        for (Libro libro : libros) {
            List<AutorDTO> autoresDTO = libro.getAutores().stream()
                    .map(autor -> new AutorDTO(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                    .collect(Collectors.toList());

            LibroDTO libroDTO = new LibroDTO(
                    libro.getId(),
                    libro.getTitulo(),
                    autoresDTO,
                    String.join(", ", libro.getIdiomas()),
                    libro.getNumeroDeDescargas()
            );

            // Mostrar la información en el formato solicitado
            System.out.println("""
                    **************************************************
                    *                      LIBRO                     *
                    **************************************************
                    Título: %s
                    Autor: %s
                    Idioma: %s
                    N° Descargas: %.2f
                    """.formatted(
                    libroDTO.titulo(),
                    libroDTO.autores().stream().map(AutorDTO::nombre).collect(Collectors.joining(", ")),
                    libroDTO.idiomas(),
                    libroDTO.numeroDeDescargas()
            ));
        }
    }

    private void listarAutoresRegistrados() {
        autores = autorRepository.findAllWithLibros();

        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados en el sistema.");
            return;
        }

        for (Autor autor : autores) {
            List<String> librosDelAutor = autor.getLibrosDelAutor().stream()
                    .map(libro -> libro.getTitulo())
                    .collect(Collectors.toList());

            AutorDTO autorDTO = new AutorDTO(
              autor.getId(),
              autor.getNombre(),
              autor.getFechaNacimiento(),
              autor.getFechaFallecimiento()
            );

            // Mostrar la información en el formato solicitado
            System.out.println("""
            
            **************************************************
            *                      AUTOR                     *
            **************************************************
            Autor: %s
            Fecha de Nacimiento: %s
            Fecha de Fallecimiento: %s
            Libros: %s
            """.formatted(
                    autorDTO.nombre(),
                    autorDTO.fechaNacimiento() != null ? autorDTO.fechaNacimiento() : "N/A",
                    autorDTO.fechaFallecimiento() != null ? autorDTO.fechaFallecimiento() : "N/A",
                    String.join(", ", librosDelAutor)
            ));
        }

    }
}
