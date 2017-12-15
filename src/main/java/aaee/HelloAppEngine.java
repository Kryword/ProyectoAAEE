package aaee;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Base64;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import com.google.protobuf.ByteString;

@SuppressWarnings("serial")
@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/hello"}
)
public class HelloAppEngine extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    response.getWriter().print("Hello App Engine!\r\n");

  }
  
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	      throws IOException {
	  response.setContentType("text/html");
	  response.setCharacterEncoding("UTF-8");
	  response.getOutputStream().print("<!DOCTYPE html>\n" + 
	  		"<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">\n" + 
	  		"\n" + 
	  		"  	<head>\n" + 
	  		"	    <link rel=\"stylesheet\" href=\"css/estilos.css\">\n" + 
	  		"	    <meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\" />\n" + 
	  		"	    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + 
	  		"	    <title>ProyectoAAEE</title>\n" + 
	  		"  	</head>\n" + 
	  		"\n" + 
	  		"  	<body>");
	  try {
		  List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
	        for (FileItem item : items) {
	            if (item.isFormField()) {
	                // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
	                String fieldName = item.getFieldName();
	                String fieldValue = item.getString();
	                response.getOutputStream().print("<div><h3 class=\"blockTitle\">Parsing field</h3><ul>"
	                		+ "<li>Field Name:" + fieldName + "\n"
            				+ "<li>Field Value:" + fieldValue + "\n</ul>"
    						+ "</div>");
	            } else {
	                // Process form file field (input type="file").
	                String fieldName = item.getFieldName();
	                String fileName = FilenameUtils.getName(item.getName());
	                String fileSize = Long.toString(item.getSize());
	                String fieldType = item.getContentType();
	                InputStream fileContent = item.getInputStream();
	                ByteString byteImg = ByteString.readFrom(fileContent);
	                byte[] img = Base64.getEncoder().encode(byteImg.toByteArray());
	                /* Muestro datos del fichero */
	                response.getOutputStream().print("<div><h3 class=\"blockTitle\">Parsing file</h3><ul>"
	                		+ "<li>Field Name:" + fieldName + "\n"
            				+ "<li>File Name:" + fileName + "\n"
    						+ "<li>File Size:" + fileSize + "\n"
							+ "<li>File Type:" + fieldType + "</ul>\n"
							+ "<h2>--- Imagen ---</h2>\n"
							+ "<img src=\"data:image/png;base64, ");
						
	                /* Muestro la imágen */
	                response.getOutputStream().write(img);
	                
	                /* Muestro el final del div */
					response.getOutputStream().print("\" alt=\"No image found\"/></div>");
	            }
	        }
	} catch (Exception e) {
		e.printStackTrace();
	}
	response.getOutputStream().print("</body></html>");
  }
}