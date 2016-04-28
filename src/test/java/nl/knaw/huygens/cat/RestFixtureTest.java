package nl.knaw.huygens.cat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import java.net.URI;
import org.assertj.core.data.MapEntry;
import org.junit.Test;
import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class RestFixtureTest {
  @Test
  public void testUrl() {
    String url = "/resources/3f4e8328-41eb-47c4-ad61-ed7d1683822b/text/xml?view=baselayer&réd=grëën&réd=black";
    Multimap<String, Object> queryParamMap = ArrayListMultimap.create();
    URI uri = URI.create(url);
    String testUrl = uri.getPath();
    String queryString = uri.getQuery();
    Splitter.on('&').trimResults().split(queryString).forEach(qkv -> {
      String[] kv = qkv.split("=");
      queryParamMap.put(kv[0], kv[1]);
    });

    assertThat("/resources/3f4e8328-41eb-47c4-ad61-ed7d1683822b/text/xml").isEqualTo(testUrl);
    assertThat(queryParamMap).containsKeys("view", "réd");
    assertThat(queryParamMap).containsValues("baselayer", "grëën");
    assertThat(queryParamMap).contains(//
        MapEntry.entry("view", "baselayer"), //
        MapEntry.entry("réd", "grëën"), //
        MapEntry.entry("réd", "black") //
        );
  }
}
