/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 31, 2014, 10:22:44 PM (GMT)]
 */
package vazkii.psi.client.core.version;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ThreadDownloadMod extends Thread {

	final String fileName;

	byte[] buffer = new byte[10240];

	int totalBytesDownloaded;
	int bytesJustDownloaded;

	InputStream webReader;

	public ThreadDownloadMod(String fileName) {
		setName("Psi Download File Thread");
		this.fileName = fileName;

		setDaemon(true);
		start();
	}

	@Override
	public void run() {
		try {
			ITextComponent component = new TranslationTextComponent("psi.versioning.startingDownload", fileName);
			if(Minecraft.getMinecraft().player != null)
				Minecraft.getMinecraft().player.sendMessage(component);

			VersionChecker.startedDownload = true;

			String base = "http://psi.vazkii.us/";
			URL url = new URL(base + "dl.php?file=" + fileName);

			try {
				url.openStream().close(); // Add to DL Counter
			} catch(IOException ignored) { }

			url = new URL(base + "files/" + fileName);
			webReader = url.openStream();

			File dir = new File(".", "mods");
			File f = new File(dir, fileName + ".dl");
			if (f.exists() || f.createNewFile()) {

				FileOutputStream outputStream = new FileOutputStream(f.getAbsolutePath());

				while ((bytesJustDownloaded = webReader.read(buffer)) > 0) {
					outputStream.write(buffer, 0, bytesJustDownloaded);
					buffer = new byte[10240];
					totalBytesDownloaded += bytesJustDownloaded;
				}
				outputStream.close();
				webReader.close();

				File f1 = new File(dir, fileName);
				if (!f1.exists() && !f.renameTo(f1)) {
					sendError();
					return;
				}

				if (Minecraft.getMinecraft().player != null)
					Minecraft.getMinecraft().player.sendMessage(new TranslationTextComponent("psi.versioning.doneDownloading", fileName).setStyle(new Style().setColor(TextFormatting.GREEN)));

				Desktop.getDesktop().open(dir);
				VersionChecker.downloadedFile = true;
			}

		} catch(Throwable e) {
			e.printStackTrace();
			sendError();
		}
	}

	private void sendError() {
		if(Minecraft.getMinecraft().player != null)
			Minecraft.getMinecraft().player.sendMessage(new TranslationTextComponent("psi.versioning.error").setStyle(new Style().setColor(TextFormatting.RED)));
	}
}
