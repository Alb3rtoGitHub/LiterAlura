package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.service.BookApiService;
import com.aluracursos.literalura.service.ConvertData;

import java.util.Scanner;

public class Principal {
    private Scanner sc = new Scanner(System.in);
    private BookApiService bookApiService = new BookApiService();
    private static final String URL_BASE = "https://gutendex.com/books";
    private ConvertData convertData = new ConvertData();



    public void muestraElMenu() {
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
            opcion = sc.nextInt();
            sc.nextLine();
            opcion = 0;

            switch (opcion) {
//                case 1:
//                    buscarLibroPorTitulo();
//                    break;
//                case 2:
//                    buscarEpisodioPorSerie();
//                    break;
//                case 3:
//                    mostrarSeriesBuscadas();
//                    break;
//                case 4:
//                    buscarSeriesPorTitulo();
//                    break;
//                case 5:
//                    buscarTop5Series();
//                    break;
//                case 6:
//                    buscarSeriesPorCategoria();
//                    break;
//                case 7:
//                    filtrarSeriesPorTemporadaYEvaluacion();
//                    break;
//                case 8:
//                    buscaEpisodiosPorTitulo();
//                    break;
//                case 9:
//                    buscarTop5Espisodios();
//                    break;

                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }

        }
    }
}
