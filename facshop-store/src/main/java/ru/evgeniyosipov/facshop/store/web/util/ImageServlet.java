package ru.evgeniyosipov.facshop.store.web.util;

import ru.evgeniyosipov.facshop.store.ejb.ProductBean;
import ru.evgeniyosipov.facshop.entity.Product;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/image/*")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 6439315738094263474L;

    @EJB
    ProductBean productBean;
    private static final Logger logger = Logger.getLogger(ImageServlet.class.getCanonicalName());

    private static final int DEFAULT_BUFFER_SIZE = 102400;

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestedImage = request.getParameter("id");

        if (requestedImage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Product p = productBean.find(Integer.parseInt(requestedImage));

        if ((p == null) || (p.getImgSrc() == null)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.reset();
            response.setBufferSize(DEFAULT_BUFFER_SIZE);
            response.setHeader("Content-Length", String.valueOf(p.getImgSrc().length));
            response.setHeader("Content-Disposition", "inline; filename=\"" + p.getName() + "\"");

            ByteArrayInputStream byteInputStream = null;
            BufferedOutputStream output = null;

            try {
                byteInputStream = new ByteArrayInputStream(p.getImgSrc());
                output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int length;
                while ((length = byteInputStream.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                }
            } finally {
                close(output);
                close(byteInputStream);
            }
        }
    }

    private static void close(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE,
                        "Возникли проблемы во время манипуляции ресурсом изображения. {0}",
                        e.getMessage());
            }
        }
    }

}
