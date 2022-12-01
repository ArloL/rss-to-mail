package io.github.arlol;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public final class ManifestVersion {

	public static String get(String name) {
		try {
			Enumeration<URL> resources = ManifestVersion.class.getClassLoader()
					.getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				Manifest manifest = new Manifest(url.openStream());
				Attributes attr = manifest.getMainAttributes();
				String version = attr.getValue("Implementation-Title");
				if (name.equals(version)) {
					version += " " + attr.getValue("Implementation-Version");
					return version;
				}
			}
			return "";
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private ManifestVersion() {
	}

}
