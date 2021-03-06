package com.naturalmotion.servlet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import csr.Compress;

public class PackServlet extends HttpServlet {

	private static final long serialVersionUID = 4444625786109757396L;

	private final Logger log = LoggerFactory.getLogger(PackServlet.class);

	private static final String SEPARATOR = "/";

	private static final String FINAL_FOLDER = "Final";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String path = new PathBuilder().build(req);
			new Compress().zipAll(path + SEPARATOR);

			File finalFolder = new File(path + SEPARATOR + FINAL_FOLDER);

			byte[] zip = zipFiles(finalFolder);
			ServletOutputStream sos = resp.getOutputStream();
			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", "attachment; filename=\"datas.zip\"");
			sos.write(zip);
			sos.flush();

		} catch (Exception e) {
			log.error("Error packing files", e);
		}
	}

	private byte[] zipFiles(File directory) throws IOException {
		String[] files = directory.list();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				for (String fileName : files) {
					writeFile(fileName, directory, zos);
				}
				zos.flush();
				baos.flush();
			}
			return baos.toByteArray();
		}
	}

	private void writeFile(String fileName, File directory, ZipOutputStream zos) throws IOException {
		byte[] bytes = new byte[2048];

		try (FileInputStream fis = new FileInputStream(directory.getPath() + "/" + fileName)) {
			try (BufferedInputStream bis = new BufferedInputStream(fis)) {
				zos.putNextEntry(new ZipEntry(fileName));

				int bytesRead;
				while ((bytesRead = bis.read(bytes)) != -1) {
					zos.write(bytes, 0, bytesRead);
				}
				zos.closeEntry();
			}
		}
	}

}
