package be.nabu.libs.resources.alias;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.URIUtils;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceResolver;

public class AliasResourceResolver implements ResourceResolver {
	
	private static Map<String, URI> aliases = new HashMap<String, URI>();
	
	static {
		String tmpDirectory = System.getProperty("java.io.tmpdir");
		if (tmpDirectory != null) {
			File file = new File(tmpDirectory);
			aliases.put("tmp", file.toURI());
		}
		for (Object key : System.getProperties().keySet()) {
			if (key.toString().startsWith("resources.alias.")) {
				String alias = key.toString().substring("resources.alias.".length());
				try {
					URI uri = new URI(URIUtils.encodeURI(System.getProperties().getProperty(key.toString())));
					aliases.put(alias, uri);
				}
				catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void alias(String alias, URI uri) {
		aliases.put(alias, uri);
	}

	@Override
	public Resource getResource(URI uri, Principal principal) {
		if (uri.getScheme() != null && aliases.containsKey(uri.getScheme())) {
			URI relativeURI = URIUtils.getChild(aliases.get(uri.getScheme()), uri.getPath());
			try {
				return ResourceFactory.getInstance().resolve(relativeURI, principal);
			}
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	public List<String> getDefaultSchemes() {
		return new ArrayList<String>(aliases.keySet());
	}

}
