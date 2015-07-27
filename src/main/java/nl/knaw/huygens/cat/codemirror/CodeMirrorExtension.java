package nl.knaw.huygens.cat.codemirror;

import nl.knaw.huygens.cat.AbstractExtension;
import org.concordion.api.extension.ConcordionExtender;

public class CodeMirrorExtension extends AbstractExtension {
  @Override
  public void addTo(ConcordionExtender concordionExtender) {
    linkCSS(concordionExtender, "/codemirror/codemirror.css");
    linkCSS(concordionExtender, "/codemirror/enable-codemirror.css");
    linkCSS(concordionExtender, "/codemirror/merge.css");

    linkJavaScript(concordionExtender, "/codemirror/codemirror.js");
    linkJavaScript(concordionExtender, "/codemirror/javascript.js");
    linkJavaScript(concordionExtender, "/codemirror/diff_match_patch.js");
    linkJavaScript(concordionExtender, "/codemirror/merge.js");
    linkJavaScript(concordionExtender, "/codemirror/enable-codemirror.js");
  }
}
