package com.naturalmotion.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalmotion.Configuration;

import csr.Compress;

public class PackServlet extends HttpServlet {

    private static final long serialVersionUID = 4444625786109757396L;

    private static final String WORKING_DIRECTORY = "working.directory";

    private static final String SEPARATOR = "/";

    private static final String FINAL_FOLDER = "Final";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String dir = req.getParameter("dir");
        try {
            Configuration configuration = new Configuration();
            new Compress().zipAll(configuration.getString(WORKING_DIRECTORY) + SEPARATOR + dir + SEPARATOR);

            File finalDir = new File(
                    configuration.getString(WORKING_DIRECTORY) + SEPARATOR + dir + SEPARATOR + FINAL_FOLDER);
            String[] list = finalDir.list();

            resp.getWriter().write(new ObjectMapper().writeValueAsString(list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dir = req.getParameter("dir");
        try {
            Configuration configuration = new Configuration();
            File backupDir = new File(configuration.getString(WORKING_DIRECTORY));
            String type = req.getParameter("type");
            writeFile(resp, dir, backupDir, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeFile(HttpServletResponse resp, String dir, File backupDir, String type) throws IOException {
        resp.setContentType("application/gzip");
        resp.setHeader("Content-Disposition", "attachment;filename=\"" +
                type +
                "\"");
        GZIPOutputStream zipStream = new GZIPOutputStream(resp.getOutputStream());
        try (FileInputStream fis = new FileInputStream(
                backupDir.getPath() + SEPARATOR + dir + SEPARATOR + FINAL_FOLDER + SEPARATOR + type);
             GZIPInputStream gis = new GZIPInputStream(fis);) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                zipStream.write(buffer, 0, len);
            }
            zipStream.flush();
        }
        resp.flushBuffer();
    }

}