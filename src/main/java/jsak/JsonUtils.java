package jsak;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class JsonUtils
{
	public static JsonElement parseJsonElement(final JsonObject json, final String field)
	{
		final JsonElement je = json.get(field);
		if (je == null)
			throw new RuntimeException("Field '" + field + "' not found");

		return je;
	}
}
