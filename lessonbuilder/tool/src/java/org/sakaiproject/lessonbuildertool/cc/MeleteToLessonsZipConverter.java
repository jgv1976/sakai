package org.sakaiproject.lessonbuildertool.cc;

/***********
 * This code is based on a reference implementation done for the IMS Consortium.
 * The copyright notice for that implementation is included below.
 * All modifications are covered by the following copyright notice.
 *
 * Copyright (c) 2016 APEREO
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2010 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

import org.springframework.web.multipart.MultipartFile;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class take the Melete CP zip file and makes it a IMS CC zip file
 * @author Javier Gómez (UPV)
 * @version 1.0
*/

public class MeleteToLessonsZipConverter {

    private MultipartFile cc;
    private String siteId;
    private Boolean displayOption; //One only page or one page per module
    public List<String> listaRecursos;
    public String rootPath;
    public String meleteFile;
    public String codigoConversion="";
    public String lessonTitle = "";

    public MultipartFile ccc; //remove when finished


    public MeleteToLessonsZipConverter(MultipartFile the_cc, String the_siteId, Boolean cpOnePage) {
        this.cc = the_cc;
        this.siteId = the_siteId;
        this.displayOption = cpOnePage;
        this.ccc = the_cc;
    }

    public MultipartFile convert() {

        File meleteFile=null;
        String rootPath = System.getenv("CATALINA_HOME") + "/temp";
        codigoConversion = cadenaRandom(3);
        lessonTitle="Lesson-"+siteId;

        try {
            meleteFile = multipartToFile(cc);
        }  catch (IOException e) {
            e.printStackTrace();
        }

        String rutaTemp = rootPath + "/temp";
        File convertido = new File(rootPath+"/convertido");
        convertido.mkdir();

        ZipTools zip = new ZipTools(meleteFile.getAbsolutePath(), rutaTemp);
        zip.unZipIt();

        try {
            listaArchivosHTML(rutaTemp);
            moverArchivos(rutaTemp, convertido);
            transformaXML(rutaTemp);
            deleteFiles(rutaTemp);
        } catch(IOException e) {
            System.err.println(e +"Hubo un error de lectura/escritura!!!");
        }

        //empaqueta los archivos procesados dentro en converted.zip
        zip.zipIt(rutaTemp, rootPath + "/converted.zip");
        deleteDir(rutaTemp);
        deleteDir(rootPath + "/convertido");
        meleteFile.delete();




        //transformar el resultado en archivo tipo multipartFile para devolverlo
/*
        File file = new File(rootPath + "/converted.zip");
        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, file.getName(), (int) file.length() , file.getParentFile());
        fileItem.getOutputStream();
        MultipartFile multipartFile = new CommonsMultipartFile(fileItem);

        return multipartFile;
*/

        
        return ccc; //remove when finished

    }


    /**
     * Metodo que realiza una lista de los archivos HTML exportados de contenidos que se encuentran 
     * en el directorio resources y necesitan ser procesados.
     * Para cada uno de los archivos de la lista se realiza una llamada a transformaHTML.
     * @param archivo El parametro archivo indica la ruta principal de los archivos y directorios exportados
     * @throws IOException
     */
    public void listaArchivosHTML(String archivo) throws IOException{
        File directorio = new File(archivo + "/resources");
        String[] archivos = directorio.list();
        for (String nombre : archivos) {
            if (nombre.contains(".html")) {
                File a = new File(directorio.getAbsolutePath()+"/"+nombre);
                if (a.isFile()) transformaHTML(a);
            }
        }
    }


    /**
     * Metodo que realiza los cambios en los archivos HTML, añadiendo cabecera y cierre html y modificando 
     * los enlaces de las imagenes para que funcionen en el servidor. Durante el proceso se crea un archivo 
     * temporal que se elimina una vez convertido.
     * @param archivo El parametro archivo indica la ruta del archivo HTML a procesar
     */
    public void transformaHTML(File archivo) throws IOException {
        String linea;
        String cabeceraHtml = "<!DOCTYPE html PUBLIC\" -//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";
        cabeceraHtml += "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n";
        cabeceraHtml += "<head>\n";
        cabeceraHtml += "<meta charset=\"UTF-8\"/>\n";
        cabeceraHtml += "</head>\n";
        cabeceraHtml += "<body>\n";
        //      cabeceraHtml += cuentaCaracter(archivo);
        String cierreHtml = "</body>\n";
        cierreHtml += "</html>";
        try {
            File convertido = new File(archivo.getAbsolutePath()+"convertido.html");
            Scanner lectura = new Scanner(archivo);
            PrintWriter escritura = new PrintWriter(convertido);
            escritura.println(cabeceraHtml);
            while(lectura.hasNext()) {
                linea = lectura.nextLine().trim();
                if (linea.contains("<img") || linea.contains("src=\"images/")) {
                    //              if (linea.contains("href=\"images")) {
                    //linea = linea.replace("images", "/access/content/group/"+ConvierteLesson.siteId+"/Export1");
                    linea = linea.replace("images", "/access/content/group/" + siteId + "/Export" + codigoConversion + "1");
                    //                  linea = linea.replace("images", "..");
                    escritura.println(linea);
                }
                else escritura.println(linea);
            }
            escritura.println(cierreHtml);
            lectura.close();
            escritura.close();
            //renombrar archivos temporales
            File renom = new File(archivo.getAbsolutePath()+"orig");
            archivo.renameTo(renom);
            convertido.renameTo(archivo);
            //Eliminar archivos temporales
            renom.delete();
        } catch(IOException e) {
            System.err.println(e +"Hubo un error de lectura/escritura!!!");
        }
    } 

    /**
     * Metodo que mueve las imagenes de la carpeta /resources/images a la ruta raiz del
     * directorio exportado de contenidos
     * @param rutaTemporal El parametro rutaTemporal indica la ruta del directorio
     * exportado de contenidos
     * @param archivo El parametro archivo indica la ruta del directorio final procesado
     */
    public void moverArchivos(String rutaTemporal, File archivo) throws IOException {
        File directorio = new File(rutaTemporal + "/resources/images");
        if(!directorio.exists()) System.out.println("No existe el directorio");
        else {
            String[] archivos = directorio.list();
            System.out.println("Lista de archivos a mover: "+archivos.length);
            for (String nombre : archivos) {
                File a = new File(directorio.getPath()+"/"+nombre);
                File destino = new File (rutaTemporal+"/"+nombre);
//              File destino = new File (archivo.getAbsolutePath()+"/"+nombre);
                a.renameTo(destino);
                System.out.println("Movida imagen \""+a.getName()+"\" al directorio : "+destino.getParent());
            } 
            String [] dir = directorio.list();
            if (dir.length == 0) 
                if (directorio.delete()) System.out.println("Borrado directorio /images");
//          File zip = new File(rutaTemporal+archivo.getName());
//          if (zip.renameTo(archivo)) System.out.println("Devuelto archivo fuente");   
        }
    }


    /**
     * Metodo que dirige la transformacion del archivo imsmanifest.xml. Inicia los archivos de lectura
     * y escritura y va llamando a los metodos que procesan las diferentes partes del archivo xml. 
     * @param ruta El parametro ruta indica la ruta del archivo imsmanifest.xml
     * que debe ser procesado
     * @throws IOException
     */
    public void transformaXML(String ruta) throws IOException {
        ruta = ruta+="/";

        try {
            File original = new File(ruta+"imsmanifest.xml");
            //guardamos en memoria la lista de recursos utilizados
            leeResources(original);
            File convertido = new File(ruta + "imsmanifestConvertido.xml");
            Scanner lectura = new Scanner(original);
            PrintWriter escritura = new PrintWriter(convertido);
            //transforma la cabecera
            procesaCabecera(escritura);
            //transforma items
            procesaItems(lectura, escritura);
            //transforma resources
            procesaResources(lectura, escritura);
            lectura.close();
            escritura.close();
            //renombrar archivos
            File origen = new File(original.getParent()+"/imsmanifestOriginal.xml");
            original.renameTo(origen);
            convertido.renameTo(original);
        } catch(IOException e) {
            System.err.println(e +"Hubo un error de lectura/escritura!!!");
        }
    }


    /**
     * Metodo que realiza una lista con los recursos contenidos en el archivo imsmanifest.xml que 
     * se utiliza para comprobar si es un recurso html o es un recurso para descargar.
     * @param xmlOriginal El parametro xmlOriginal se refiere al archivo imsmanifest.xml exportado
     * desde contenidos.
     * @throws FileNotFoundException
     */
    private void leeResources(File xmlOriginal) throws FileNotFoundException {
        listaRecursos = new ArrayList<String>();
        Scanner lectura = new Scanner(xmlOriginal);
        Boolean recursos = false;
        String linea;
        while (lectura.hasNext() && !recursos) {
            linea = lectura.nextLine();
            if (linea.contains("<resources>")) recursos = true;
        }
        while (lectura.hasNext()) {
            linea = lectura.nextLine();
            listaRecursos.add(linea);
        }
        lectura.close();
    }


    /**
     * Inicia el nuevo documento imsmanifest.xml escribiendo la cabecera adecuada para 
     * lesson builder. Tambien introduce la etiqueta inicial <item> que recoje todo el 
     * contenido de <organization>
     * @param escribe El parametro escribe recibe el objeto de escritura sobre el nuevo archivo xml 
     */
    private void procesaCabecera(PrintWriter escribe) {


        escribe.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        escribe.println("<manifest identifier=\"cctd0001\"" );
        escribe.println("xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1\"");
        escribe.println("xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource\"");
        escribe.println("xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest\"");
        escribe.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        escribe.println("xsi:schemaLocation=\" http://ltsc.ieee.og/xsd/imsccv1p1/LOM/resource ");
        escribe.println("http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imscp_v1p2_v1p0.xsd ");
        escribe.println("http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lommanifest_v1p0.xsd\"> ");
        escribe.println("<metadata> ");
        escribe.println("<schema>IMS Common Cartridge</schema> " );  
        escribe.println("<schemaversion>1.3.0</schemaversion> " );   
        escribe.println("<lomimscc:lom> ");
        escribe.println("<lomimscc:general>");
        escribe.println("<lomimscc:title>");
        escribe.println("<lomimscc:string language=\"en-US\">Export" + codigoConversion + "</lomimscc:string> ");
        escribe.println("</lomimscc:title>");
        escribe.println("</lomimscc:general>");
        escribe.println("</lomimscc:lom>");
        escribe.println("</metadata> ");
        escribe.println("<organizations> " );
        escribe.println("<organization identifier=\"Org1\" structure=\"rooted-hierarchy\"> ");
        escribe.println("<item>");
        if (displayOption) {
            escribe.println("<item>");
            escribe.println("<title>"+ lessonTitle +"</title>");
        }
    }


    /**
     * Metodo que procesa la parte del archivo imsmanifest.xml referente a los item (unidades, secciones).
     * Elimina todas las etiquetas <imsmd...> que no son necesarias y añade nuevas definiciones de items y 
     * bloques de <metadata> necesarios para la visualizacion del contenido en lesson builder. 
     * Realiza tambien la comprobacion del tamaño maximo de los titulos de unidades y secciones llamando 
     * al metodo compruebaLongitudTitulo().
     * @param lectura El parametro lectura recibe el objeto de lectura sobre el archivo imsmanifest.xml original
     * @param escritura El parametro escritura recibe el objeto de escritura para continuar sobre el nuevo archivo xml
     * @throws IOException
     */
    private void procesaItems(Scanner lectura, PrintWriter escritura) throws IOException {

        String linea;
        String lineaSiguiente;
        String metadata="";
        String recurso;
        Boolean oculto = false;
        Boolean escribe = false;
        Boolean noResource = true;
        metadata = "\t<metadata>\n";
        metadata += "\t\t<lom:lom>\n";
        metadata += "\t\t\t<lom:general>\n";
        metadata += "\t\t\t\t<lom:structure>\n";
        metadata += "\t\t\t\t\t<lom:source>inline.lessonbuilder.sakaiproject.org</lom:source>\n";
        metadata += "\t\t\t\t\t<lom:value>true</lom:value>\n";
        metadata += "\t\t\t\t</lom:structure>\n";
        metadata += "\t\t\t</lom:general>\n";
        metadata += "\t\t</lom:lom>\n";
        metadata += "\t</metadata>";

        while(lectura.hasNext() && noResource) {

            linea = lectura.nextLine();

            if (linea.contains("isvisible=\"false\"")) oculto = true;
            if (linea.contains("<title>")) linea = compruebaLongitudTitulo(linea);
            if (linea.trim().startsWith("<item")) escribe = true;

            if (escribe){
                //              if (linea.contains("<item identifier=\"MF")) itemUnidad = true;

                //              if (itemUnidad && linea.contains("identifierref")) {
                if (linea.contains("identifierref") && !oculto) {
                    recurso = linea.substring(linea.indexOf("RESOURCE"), linea.indexOf("\">"));

                    if (esContenido(recurso)) {

                        System.out.println(recurso + " es contenido para visualizar.");

                        lineaSiguiente = lectura.nextLine();
                        if (lineaSiguiente.contains("<title>")) lineaSiguiente = compruebaLongitudTitulo(lineaSiguiente);
                        escritura.println(linea.substring(0, linea.indexOf("identifierref"))+">");
                        escritura.println(lineaSiguiente);
                        escritura.println(linea.replace(linea.substring(linea.indexOf("r=\""), linea.indexOf("r=\"")+3),"=\"x"));
                        escritura.println(lineaSiguiente);
                        escritura.println(metadata);
                        escritura.println("</item>");
                    }
                    else {
                        System.out.println(recurso + " es contenido DESCARGABLE.");
                        escritura.println(linea);
                    }
                }
                else if(linea.contains("</organization>")) {
                    escritura.println("</item>");
                    if (displayOption) escritura.println("</item>");
                    escritura.println("</organization>");
                    noResource = false;
                }
                else if (linea.indexOf("imsmd") == -1 && !oculto) escritura.println(linea);
            }
        }
    }


    /**
     * Metodo que comprueba si el recurso referido en imsmanifest.xml se corresponde con un html para visualizar
     * o se trata de un pdf u otro tipo de archivo descargable.
     * @param recurso
     * @return
     */
    private Boolean esContenido(String recurso) {
        for (String r : listaRecursos) {
            if (r.contains(recurso) && r.contains(".html")) 
                return true;
        }
        return false;
    }



    /**
     * Metodo que comprueba que los titulos de las unidades y secciones no superen los 100 caracteres
     * como maximo admitidos por el campo title de la base de datos.
     * @param titulo El parametro titulo contiene el string con el titulo a comprobar
     * @return El titulo original si no supera los 100 caracteres o el titulo recortado a 95 caracteres si lo supera
     */
    private String compruebaLongitudTitulo(String titulo) {

        if (titulo.length()>114){
            String tituloMayor100 = titulo.substring(titulo.indexOf(">")+1, titulo.indexOf("</")-1);
            System.out.println("Titulo mayor de 100 caracteres: " + tituloMayor100 + " (longitud: "+titulo.length()+")");
            String tituloMenor100 = tituloMayor100.substring(0,94);
            System.out.println("Titulo menor de 100 caracteres: "+ tituloMenor100 +" (longitud: "+titulo.length()+")");
            titulo = "<title>"+tituloMenor100+"</title>";
            System.out.println("Resultado de linea final: "+titulo);
        }
        return titulo;
    }


    /**
     * Metodo que realiza el procesado de la parte de <resources> del archivo imsmanifest.xml. Para cada imagen enlazada 
     * se crea un nuevo recurso y una dependencia a ella, eliminando las lineas que contienen imsd que son inservibles.
     * @param lectura El parametro lectura recibe el objeto de lectura sobre el archivo imsmanifest.xml original
     * @param escritura El parametro escritura recibe el objeto de escritura para continuar sobre el nuevo archivo xml
     * @throws IOException
     */
    private void procesaResources(Scanner lectura, PrintWriter escritura) throws IOException {
        int contador=0;
        ArrayList<String> listaResources = new ArrayList<String>();
        String linea;
        String resource="";
        String enlaceImagen;
        System.out.println("\n\nProcesando recursos..................\n\n");
        while(lectura.hasNext()) {
            linea = lectura.nextLine().trim();
            if (linea.contains("<file") && linea.contains("images")) {
                escritura.println("<dependency identifierref=\"res"+contador+"\"/>");
                enlaceImagen = linea.replace("<file href=\"resources/images/", "href=\"");
                resource += "<resource type=\"webcontent\" identifier=\"res"+contador+"\" "+enlaceImagen.substring(0, enlaceImagen.length()-2)+">\n";
                resource += linea.replace("href=\"resources/images/", "href=\"")+"\n";
                resource += "</resource>\n";
                listaResources.add(resource);
                resource = "";
                contador++;
            }
            else if(linea.contains("</resources>")) {
                for (String recurso : listaResources) {
                    escritura.println(recurso);
                }
                escritura.println(linea);
            }
            else if(!linea.contains("imsm")) escritura.println(linea);
        }
    } 

    /**
     * Metodo que borra archivos .xsd creados en la exportacion de CONTENIDOS 
     * y el archivo temporal imsmanifest.xml que no son necesarios.
     * @param ruta El parametro ruta indica la ruta principal del directorio temporal 
     * CONVERTIDO creado para el procesado de los archivos
     */
    public void deleteFiles(String ruta) {
        new File(ruta + "/imscp_v1p1.xsd").delete();
        new File(ruta + "/imsmd_v1p2.xsd").delete();
        new File(ruta + "/imsmanifestOriginal.xml").delete();
        new File(ruta + "/xml.xsd").delete();    
    }


    /**
    * Metodo que crea una cadena aleatoria para identificar la ruta de los recursos de cada 
    * importacion. Esto permite importar diferentes modulos melete en un mismo lessons
    * @param longitud indica la longitud de la cadena que se crea  
    */
    private String cadenaRandom (int longitud){
        String cadenaAleatoria = "";
        long milis = new java.util.GregorianCalendar().getTimeInMillis();
        Random r = new Random(milis);
        int i = 0;
        while ( i < longitud){
            char c = (char)r.nextInt(255);
            if ( (c >= '0' && c <='9') /*|| (c >='A' && c <='Z')*/ ){
                cadenaAleatoria += c;
                i ++;
            }
        }
        return cadenaAleatoria;
    }


    /**
     * Metodo que borra una carpeta y todo su contenido. Utiliza el metodo privado deleteChildren()
     * @param ruta El parametro ruta indica la ruta de la carpeta a borrar
     * @return Devuelve true o false segun el resultado del borrado de la carpeta
     */
    public boolean deleteDir(String ruta) { 

        File file = new File(ruta);  
        if (!file.exists()) {  
            return true;  
        }  
        if (!file.isDirectory()) {  
            return file.delete();  
        }  
        return this.deleteChildren(file) && file.delete();  
    }  

    
    /**
     * Metodo privado utilizado por deleteDir() para eliminar archivos contenidos en un directorio
     * @param dir El parametro dir recibe la ruta de la carpeta que contiene los archivos a borrar
     * @return true o false segun el resultado del borrado del archivo
     */
    private boolean deleteChildren(File dir) {  
        File[] children = dir.listFiles();  
        boolean childrenDeleted = true;  
        for (int i = 0; children != null && i < children.length; i++) {  
            File child = children[i];  
            if (child.isDirectory()) {  
                childrenDeleted = this.deleteChildren(child) && childrenDeleted;  
            }  
            if (child.exists()) {  
                childrenDeleted = child.delete() && childrenDeleted;  
            }  
        }  
        return childrenDeleted;  
    } 


    /**
     * Metodo que transforma un archivo tipo multipartFile en tipo File
     * @param multipart El parametro multipart recibe el archivo .zip de melete de tipo multipartFile. 
     */
    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + multipart.getOriginalFilename());
        multipart.transferTo(tmpFile);
        return tmpFile;
    }

}

