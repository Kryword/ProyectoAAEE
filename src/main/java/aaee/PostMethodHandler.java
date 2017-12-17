package aaee;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.protobuf.ByteString;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;

@SuppressWarnings("serial")
@WebServlet(
    name = "PostMethodHandler",
    urlPatterns = {"/traduce"}
)
public class PostMethodHandler extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	      throws IOException {
	  response.setContentType("text/html; charset=UTF-8");
	  response.setCharacterEncoding("UTF-8");
	  PrintWriter out = response.getWriter();
	  out.print("<!DOCTYPE html>\n" + 
	  		"<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">\n" + 
	  		"\n" + 
	  		"  	<head>\n" + 
	  		"	    <link rel=\"stylesheet\" href=\"css/estilos.css\">\n" + 
	  		"	    <meta charset=\"UTF-8\">\n" + 
	  		"	    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no\">\n" + 
	  		"	    <title>ProyectoAAEE</title>\n" + 
	  		"  	</head>\n" + 
	  		"\n" + 
	  		"  	<body>"
	  		+ "<header><h1 id=\"title\">Resultados</h1></header>");
	  try {
		  List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		  String lang = "en"; //English by default
		  InputStream fileContent = null;
		  long fileSizeLong = 0;
	        for (FileItem item : items) {
	            if (item.isFormField()) {
	                // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
	                String fieldName = item.getFieldName();
	                String fieldValue = item.getString();
	                if (fieldName.equals("lang") && !fieldValue.isEmpty()) {
	                	lang = fieldValue;
	                }
	            } else {
	                // Proceso el campo del fichero, de aquí obtengo la imágen que proceso después
	                fileSizeLong = item.getSize();
	                fileContent = item.getInputStream();
	                
	            }
	        }
	        
	        //Procesamiento del fichero
	        /* Muestro datos del fichero */
	        if(fileSizeLong > 3000000) {
            	out.print("<h2>Imágen introducida supera los 3MB permitidos. Introduzca una imágen de menor tamaño.</h2></div>");
            	out.print("</body></html>");
            	return;
            }
	        if(fileSizeLong == 0) {
            	out.print("<h2>Error: No ha introducido ningún archivo.</h2></div>");
            	out.print("</body></html>");
            	return;
            }
	        ByteString byteImg = ByteString.readFrom(fileContent);
	        fileContent.close();
            out.print("<div id=\"postimagediv\">"
					+ "<h3>Imagen</h2>\n"
					+ "<img id=\"img\"src=\"data:image/png;base64, ");
				
            /* Muestro la imágen */
            out.print(Base64.getEncoder().encodeToString(byteImg.toByteArray()));
            
            /* Muestro el final del div */
			out.print("\" alt=\"No image found\"/>");
			
			// Detección de texto dentro de la imágen
			ImageAnnotatorClient vision = ImageAnnotatorClient.create();
		    
		    List<AnnotateImageRequest> requests = new ArrayList<>();
		    Image sendImage = Image.newBuilder().setContent(byteImg).build();
		    Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
		    Feature featText = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
		    AnnotateImageRequest requestForVision = AnnotateImageRequest.newBuilder().addFeatures(feat).addFeatures(featText).setImage(sendImage).build();
		    requests.add(requestForVision);
		    //Envío la request y recojo la respuesta en responseVision
		    BatchAnnotateImagesResponse responseVision = vision.batchAnnotateImages(requests);
		    List<AnnotateImageResponse> responses = responseVision.getResponsesList();
		    String textOCR = "";
		    //Recorro todas las respuestas recibidas
		    for (AnnotateImageResponse res: responses) {
			  if (res.hasError()) {
				    System.out.printf("Error: %s\n", res.getError().getMessage());
					return;	
			  }
			  out.print("<h3>Lista de labels</h3><ul>");
			  for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
				  out.print("<li>" + annotation.getDescription() + "\n");
			  }
			  textOCR = res.getFullTextAnnotation().getText();
			  out.print("</ul><h3>Texto leído</h3>\n<p id=\"textOCR\">" + textOCR + "</p>");
		    }
		    
		    byteImg = null;
		    
		    Translate translate = TranslateOptions.getDefaultInstance().getService();
		    Translation translation = translate.translate(textOCR, TranslateOption.targetLanguage(lang));
		    out.print("<h3>Traducción</h3><p>" + translation.getTranslatedText() + "</p>");
		    out.print("</div>");
	} catch (Exception e) {
		e.printStackTrace();
	}
	out.print("</body></html>");
  }
}