package io.spring.up.tool.io;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.spring.up.atom.JsonArray;
import io.spring.up.atom.JsonObject;
import io.spring.up.cv.Constants;
import io.spring.up.cv.Encodings;
import io.spring.up.cv.Strings;
import io.spring.up.exception.internal.EmptyStreamException;
import io.spring.up.tool.fn.Fn;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("all")
class IO {

    private static final ObjectMapper YAML = new YAMLMapper();

    static JsonArray getJArray(final String filename) {
        return Fn.getJvm(new JsonArray(), () -> {
            final String content = getString(filename);
            return null == content ? new JsonArray() : new JsonArray(content);
        }, filename);
    }

    static JsonObject getJObject(final String filename) {
        return Fn.getJvm(new JsonObject(), () -> {
            final String content = getString(filename);
            return null == content ? new JsonObject() : new JsonObject(content);
        }, filename);
    }

    static String getString(final String filename) {
        return Fn.getJvm(Strings.EMPTY, () -> {
            final InputStream in = getStream(filename);
            return null == in ? Strings.EMPTY : getContent(in);
        }, filename);
    }

    static Properties getProp(final String filename) {
        return Fn.getJvm(new Properties(), () -> {
            final InputStream in = getStream(filename);
            final Properties prop = new Properties();
            if (null != in) {
                prop.load(in);
                in.close();
            }
            return prop;
        }, filename);
    }

    static InputStream getStream(final String filename) {
        final File file = getFile(filename);
        return Fn.getJvm(null, () -> FileUtil.getInputStream(file), file);
    }

    static File getFile(final String filename) {
        final File file = new File(filename);
        if (file.exists()) {
            // 当前路径
            return file;
        } else {
            // 检索路径
            final URL url = getURL(filename);
            return (null == url) ? null : new File(url.getFile());
        }
    }

    static URL getURL(final String filename) {
        return Fn.getJvm(() -> {
            // 当前类路径
            URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
            if (null == url) {
                // 当前IO类路径
                url = IO.class.getResource(filename);
            }
            // Spring Docker容器路径
            if (null == url) {
                url = new ClassPathResource(filename).getURL();
            }
            return url;
        }, filename);
    }

    static <T> T getJYaml(final String filename) {
        final boolean isArray = isJArray(filename);
        return isArray ? (T) new JsonArray(getYamlNode(filename).toString()) :
                (T) new JsonObject(getYamlNode(filename).toString());
    }

    private static JsonNode getYamlNode(final String filename) {
        final InputStream in = getStream(filename);
        if (null == in) {
            // 双重检查，上层要转换
            throw new EmptyStreamException(filename);
        }
        final JsonNode node = Fn.getJvm(() -> YAML.readTree(in));
        if (null == node) {
            // 双重检查，上层要转换
            throw new EmptyStreamException(filename);
        }
        return node;
    }

    private static boolean isJArray(final String filename) {
        final String content = getString(filename);
        return Fn.getJvm(Boolean.FALSE, () -> content.trim().startsWith(Strings.DASH), content);
    }

    private static String[] getLines(final InputStream in) {
        final List<String> lineList = new ArrayList<>();
        return Fn.getJvm(lineList.toArray(new String[]{}), () -> {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Encodings.UTF_8));
            String line;
            while (null != (line = reader.readLine())) {
                lineList.add(line);
            }
            reader.close();
            return lineList.toArray(new String[]{});
        }, in);
    }

    private static String getContent(final InputStream in) {
        final StringBuilder buffer = new StringBuilder(Constants.DEFAULT_BUFFER_SIZE);
        final String[] lines = getLines(in);
        for (final String line : lines) {
            buffer.append(line);
        }
        return buffer.toString();
    }
}