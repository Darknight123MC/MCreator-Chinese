/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2020 Pylo and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.minecraft;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import net.mcreator.io.FileIO;
import net.mcreator.plugin.PluginLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataListLoader {

	private static final Logger LOG = LogManager.getLogger("Data List Loader");

	private static final Map<String, LinkedHashMap<String, DataListEntry>> cache = new HashMap<>();

	public static void preloadCache() {
		Set<String> fileNames = PluginLoader.INSTANCE.getResources("datalists", Pattern.compile(".*\\.yaml"));
		for (String res : fileNames) {
			String datalistname = res.split("datalists/")[1].replace(".yaml", "");
			loadDataList(datalistname);
		}
	}

	public static Map<String, LinkedHashMap<String, DataListEntry>> getCache() {
		return cache;
	}

	public static List<DataListEntry> loadDataList(String listName) {
		return new ArrayList<>(loadDataMap(listName).values());
	}

	public static Map<String, DataListEntry> loadDataMap(String listName) {
		if (cache.get(listName) != null)
			return cache.get(listName);

		AtomicReference<LinkedHashMap<String, DataListEntry>> list = new AtomicReference<>();
		list.set(new LinkedHashMap<>());

		try {
			Enumeration<URL> res = PluginLoader.INSTANCE.getResources("datalists/" + listName + ".yaml");
			Collections.list(res).forEach(resource -> {
				String config = FileIO.readResourceToString(resource);

				YamlReader reader = new YamlReader(config);
				try {
					((List<?>) reader.read()).forEach(elementObj -> {
						if (elementObj instanceof String) {
							if (list.get().containsKey(elementObj))
								LOG.warn("重复的datalist的key为: " + elementObj);
							list.get().put((String) elementObj, new DataListEntry((String) elementObj));
						} else if (elementObj instanceof Map<?, ?> element) {
							String elementName = null;
							for (Map.Entry<?, ?> entry : element.entrySet())
								if (entry.getValue() == null)
									elementName = (String) entry.getKey();

							if (elementName != null) {
								DataListEntry entry = new DataListEntry(elementName);
								entry.setReadableName((String) element.get("readable_name"));
								entry.setType((String) element.get("type"));
								entry.setDescription((String) element.get("description"));
								entry.setOther(element.get("other"));
								entry.setTexture((String) element.get("texture"));

								if (element.get("required_apis") instanceof List)
									entry.setRequiredAPIs(
											((List<?>) element.get("required_apis")).stream().map(Object::toString)
													.collect(Collectors.toList()));

								if (listName.equals("blocksitems")) {
									MCItem mcitem = new MCItem(entry);
									if (element.get("subtypes") != null) {
										mcitem.setSubtypes(Boolean.parseBoolean((String) element.get("subtypes")));
									}
									if (list.get().containsKey(elementName))
										LOG.warn("重复的datalist的key为: " + elementName);
									list.get().put(elementName, mcitem);
								} else {
									if (list.get().containsKey(elementName))
										LOG.warn("重复的datalist的key为: " + elementName);
									list.get().put(elementName, entry);
								}
							}
						}
					});
				} catch (YamlException e) {
					LOG.error(e.getMessage(), e);
				}
			});
		} catch (IOException e) {
			LOG.error("载入datalist失败", e);
		}

		LOG.debug("已经添加 " + listName + " 数据映射到缓存");

		cache.put(listName, list.get());

		return list.get();
	}

}
