package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.dto.AutorDTO;
import com.aluracursos.literalura.dto.LibroDTO;
import com.aluracursos.literalura.model.*;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;

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
                case 4:
                    listarAutoresVivosEnAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }

        }
    }

    // Encuentra el primer titulo con el que se busca
    private void buscarLibroPorTitulo() {
        DatosLibros datosLibros = getDatosLibros();

        if (datosLibros == null) {
            System.out.println("""
                    
                    **************************************************
                    Libro no Encontrado.
                    **************************************************""");
            return;
        }

        // Verificar si el libro ya existe en la base de datos
        Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibros.titulo());
        if (libroExistente.isPresent()) {
            System.out.println("""
                    
                    **************************************************
                    El libro ya está registrado en el sistema.
                    **************************************************""");
            return;
        }

        // Si el libro existe procesamos los autores
        List<Autor> autores = datosLibros.autor().stream()
                .map(datosAutor -> autorRepository.findByNombre(datosAutor.nombre())
                        .orElseGet(() -> {
                            // Crear y guardar nuevo autor
                            Autor nuevoAutor = new Autor();
                            nuevoAutor.setNombre(datosAutor.nombre());
                            nuevoAutor.setFechaNacimiento(datosAutor.fechaNacimiento());
                            nuevoAutor.setFechaFallecimiento(datosAutor.fechaFallecimiento());
                            autorRepository.save(nuevoAutor);
                            return nuevoAutor;
                        })
                ).collect(Collectors.toList());

        // Crear el libro con los datos de datosLibros y añadir los autores
        Libro libro = new Libro(datosLibros);
        libro.setAutores(autores);
        // Guardar el libro junto con sus autores en la base de datos
        libroRepository.save(libro);

        // Crear y mostrar DTO del libro guardado
        LibroDTO libroDTO = new LibroDTO(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutores().stream().map(autor -> new AutorDTO(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                        .collect(Collectors.toList()),
                String.join(", ", libro.getIdiomas()),
                libro.getNumeroDeDescargas()
        );

        // Imprimir detalles del libro registrado
        System.out.printf(
                """
                        
                        **************************************************
                        *                      LIBRO                     *
                        **************************************************
                        Título: %s
                        Autor: %s
                        Idioma: %s
                        N° Descargas: %.2f%n""", libroDTO.titulo(),
                libroDTO.autores().stream().map(AutorDTO::nombre).collect(Collectors.joining(", ")),
                libroDTO.idiomas(),
                libroDTO.numeroDeDescargas()
        );
        System.out.println("--------------------------------------------------");
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

        // Encontrar el primer libro coincidente en la lista de resultados
        return datosBusqueda.listaResultados().stream()
                .filter(datosLibros -> datosLibros.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst()
                .orElse(null); // Devolver null si no se encuentra el libro
    }

    private void listarLibrosRegistrados() {
        libros = libroRepository.findAllWithAutores();

        if (libros.isEmpty()) {
            System.out.println("""
                    
                    **************************************************
                    No hay Libros registrados en el sistema.
                    **************************************************""");
            return;
        }
        System.out.printf("""
                
                **************************************************
                *            %d LIBROS REGISTRADOS               *
                **************************************************%n""", libros.size());
        mostrarLibros(libros);
    }

    private void listarAutoresRegistrados() {
        autores = autorRepository.findAllWithLibros();

        if (autores.isEmpty()) {
            System.out.println("""
                    
                    **************************************************
                    No hay Autores registrados en el sistema.
                    **************************************************""");
            return;
        }
        System.out.printf("""
                
                **************************************************
                *            %d AUTORES REGISTRADOS              *
                **************************************************%n""", autores.size());
        mostrarAutores(autores);
    }

    private void listarAutoresVivosEnAnio() {
        var valorValido = false;
        String anioEstaVivo;
        do {
            System.out.print("Ingresa el año para buscar autores vivos en ese período: ");
            anioEstaVivo = sc.nextLine();

            // Validar que el año ingresado tenga 4 dígitos numéricos
            if (!anioEstaVivo.matches("\\d{4}")) {
                System.out.println("""
                        
                        **************************************************
                        Año no válido. Por favor, ingresa un año de 4 dígitos.
                        **************************************************
                        """);
                continue;
            }
            valorValido = true;
        } while (!valorValido);

        int anio = Integer.parseInt(anioEstaVivo);

        // Obtener autores vivos en el año especificado
        List<Autor> autoresVivos = autorRepository.findByFechaNacimientoBeforeAndFechaFallecimientoAfter(String.valueOf(anio), String.valueOf(anio));

        if (autoresVivos.isEmpty()) {
            System.out.println("""
                    
                    **************************************************
                    No se encontraron autores vivos en el año especificado.
                    **************************************************
                    """);
        } else {
            System.out.printf("""
                    
                    **************************************************
                    *            %d AUTORES VIVOS EN %d               *
                    **************************************************%n""", autoresVivos.size(), anio);
            mostrarAutores(autoresVivos);
        }
    }

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>COPIAR AL TXT DESDE ACA 1/11/24<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    private void listarLibrosPorIdioma() {
        String idiomaLibro;
        do {
            System.out.print("Ingresa el código del idioma del Libro a buscar [2 letras, ej: es]: ");
            idiomaLibro = sc.nextLine().toLowerCase();

            // Validar que el idioma ingresado tenga dos letras y no incluya números
            if (!idiomaLibro.matches("^[a-z]{2}$")) {
                System.out.println("""
                        
                        **************************************************
                        Código de idioma no válido. Debe ser un código de 2 letras.
                        **************************************************
                        """);
            }
        } while (!idiomaLibro.matches("^[a-z]{2}$"));

        // Lista de libros en idioma buscado
        List<Libro> librosPorIdioma = libroRepository.findByIdiomasContaining(idiomaLibro);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("""
                    
                    **************************************************
                    No se encontraron Libros en el Idioma buscado.
                    **************************************************
                    """);
        } else {
            if (librosPorIdioma.size() == 1) {
                System.out.printf("""
                        
                        **************************************************
                        *           %d LIBRO EN EL IDIOMA '%s'            *
                        **************************************************%n""", librosPorIdioma.size(), idiomaLibro.toUpperCase());
                mostrarLibros(librosPorIdioma);
            } else {
                System.out.printf("""
                        
                        **************************************************
                        *           %d LIBROS EN EL IDIOMA '%s'           *
                        **************************************************%n""", librosPorIdioma.size(), idiomaLibro.toUpperCase());
                mostrarLibros(librosPorIdioma);
            }
        }
    }

    private void mostrarLibros(List<Libro> libroList) {
        for (Libro libro : libroList) {
            List<AutorDTO> autoresDTO = libro.getAutores().stream()
                    .map(autor -> new AutorDTO(autor.getId(), autor.getNombre(), autor.getFechaNacimiento(), autor.getFechaFallecimiento()))
                    .collect(Collectors.toList());

            // Crear el DTO para mostrar solo la información necesaria
            LibroDTO libroDTO = new LibroDTO(
                    libro.getId(),
                    libro.getTitulo(),
                    autoresDTO,
                    String.join(", ", libro.getIdiomas()),
                    libro.getNumeroDeDescargas()
            );

            System.out.printf(
                    """
                            
                            **************************************************
                            *                      LIBRO                     *
                            **************************************************
                            Título: %s
                            Autor: %s
                            Idioma: %s
                            N° Descargas: %.2f%n""", libroDTO.titulo(),
                    libroDTO.autores().stream().map(AutorDTO::nombre).collect(Collectors.joining(", ")),
                    String.join(", ", libro.getIdiomas()),
                    libroDTO.numeroDeDescargas()
            );
            System.out.println("--------------------------------------------------");

        }
    }

    private void mostrarAutores(List<Autor> autoresList) {
        for (Autor autor : autoresList) {
            List<String> librosDelAutor = autor.getLibrosDelAutor().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.toList());

            AutorDTO autorDTO = new AutorDTO(
                    autor.getId(),
                    autor.getNombre(),
                    autor.getFechaNacimiento(),
                    autor.getFechaFallecimiento()
            );

            // Mostrar la información en el formato solicitado
            System.out.printf(
                    """
                            
                            **************************************************
                            *                      AUTOR                     *
                            **************************************************
                            Autor: %s
                            Fecha de Nacimiento: %s
                            Fecha de Fallecimiento: %s
                            Libros: %s%n""", autorDTO.nombre(),
                    autorDTO.fechaNacimiento() != null ? autorDTO.fechaNacimiento() : "N/A",
                    autorDTO.fechaFallecimiento() != null ? autorDTO.fechaFallecimiento() : "N/A",
                    librosDelAutor
            );
            System.out.println("--------------------------------------------------");
        }
    }
}
