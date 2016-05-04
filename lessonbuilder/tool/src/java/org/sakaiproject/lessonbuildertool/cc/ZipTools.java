package org.sakaiproject.lessonbuildertool.cc;

import java.io.File;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTools {
    
    List<String> fileList;
    public String INPUT_ZIP_FILE;
    public String OUTPUT_FOLDER;
    public String SOURCE_FOLDER;
    
    public ZipTools(String input_file, String output_file ) {
        
        this.INPUT_ZIP_FILE = input_file;
        this.OUTPUT_FOLDER = output_file;
    }
    
    /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    public void unZipIt(){

        byte[] buffer = new byte[1024];
            
        try{  
            File folder = new File(OUTPUT_FOLDER);
            if(!folder.exists()){
                folder.mkdir();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(INPUT_ZIP_FILE));
            ZipEntry ze = zis.getNextEntry();
                
            while(ze!=null){
                    
                String fileName = ze.getName();
                File newFile = new File(OUTPUT_FOLDER + File.separator + fileName);
                    
                System.out.println("file unzip : "+ newFile.getAbsoluteFile());
                    
                new File(newFile.getParent()).mkdirs();
                  
                FileOutputStream fos = new FileOutputStream(newFile);             

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }                   
                fos.close();   
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();     
        }catch(IOException ex){
           ex.printStackTrace(); 
        }
    }
    
    
    /**
     * Metodo para comprimir todos los archivos necesarios en un fichero zip listo para importar desde lessons
     * Utiliza el metodo generateFileList() para generar los nombres de los archivos que se incluiran en el zip
     * @param zipFile el parametro zipFile recibe el nombre/ruta del archivo zip que se genera despues de la conversion
     */
    public void zipIt(String rutaTemp, String zipFile){
        
        fileList = new ArrayList<String>();
        SOURCE_FOLDER = rutaTemp;
        generateFileList(new File(rutaTemp));
        
        byte[] buffer = new byte[1024];
        try{
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for(String file : this.fileList){

                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(SOURCE_FOLDER + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            }
            zos.closeEntry();
            zos.close();

        }catch(IOException ex){
            ex.printStackTrace();   
        }
    }

    
    /**
     * Metodo que genera una lista con los archivos que seran incluidos en el zip
     * Utiliza el metodo privado generateZipEntry() para generar el nombre / ruta de los archivos
     * @param node El parametro node indica la ruta principal de la carpeta temporal donde estan los archivos del sitio
     */
    public void generateFileList(File node){

        if(node.isFile()){
            //con el replace cambiamos la contrabarra de la ruta de los archivos por barra normal para que
            //se reconozcala carpeta al abrirlo en linux
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString().replace("\\", "/"))); 
        }

        if(node.isDirectory()){
            String[] subNote = node.list();
            for(String filename : subNote){
                generateFileList(new File(node, filename));
            }
        }
    }

    
    /**
     * Metodo que genera el nombre con el que sera comprimido cada archivo a partir de su ruta absoluta
     * @param file El parametro file recibe la ruta absoluta de cada uno de los archivos que se incluiran 
     * en la lista de archivos a comprimir
     * @return Devuelve el nombre/ruta local del archivos para incluir en el zip
     */
    private String generateZipEntry(String file){
        System.out.println(file.substring(SOURCE_FOLDER.length(), file.length()));
        return file.substring(SOURCE_FOLDER.length(), file.length());
    }
}

