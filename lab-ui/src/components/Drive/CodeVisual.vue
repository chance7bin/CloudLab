<template>
  <Codemirror
      style="margin-bottom: -7px;font-size: 13px"
      v-model:value="modelValue"
      :options="cmOptions"
      border
      placeholder="test placeholder"
  />
</template>

<script setup>
import {computed, ref} from 'vue'

import Codemirror from "codemirror-editor-vue3";
// language
import "codemirror/mode/xml/xml.js";

import 'codemirror/addon/scroll/simplescrollbars.css'
import 'codemirror/addon/scroll/simplescrollbars'

// theme
import 'codemirror/theme/base16-light.css'
import 'codemirror/theme/mdn-like.css'
import 'codemirror/theme/idea.css'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// foldGutter
import 'codemirror/addon/fold/foldgutter.css'
import 'codemirror/addon/fold/brace-fold.js'
import 'codemirror/addon/fold/comment-fold.js'
import 'codemirror/addon/fold/foldcode.js'
import 'codemirror/addon/fold/foldgutter.js'
import 'codemirror/addon/fold/indent-fold.js'
import 'codemirror/addon/fold/markdown-fold.js'
import 'codemirror/addon/fold/xml-fold.js'
// autoCloseTags
import 'codemirror/addon/edit/closetag.js'
// hint
import 'codemirror/addon/hint/show-hint.js'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/javascript-hint.js'
import 'codemirror/addon/hint/xml-hint.js'
import 'codemirror/addon/selection/active-line.js'
// highlightSelectionMatches
import 'codemirror/addon/scroll/annotatescrollbar.js'
import 'codemirror/addon/search/matchesonscrollbar.js'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/match-highlighter.js'


const props = defineProps(['modelValue'])


function completeAfter(cm, pred) {
  var cur = cm.getCursor();
  if (!pred || pred()) setTimeout(function () {
    if (!cm.state.completionActive)
      cm.showHint({completeSingle: false});
  }, 100);
  return CodeMirror.Pass;
}

function completeIfAfterLt(cm) {
  return completeAfter(cm, function () {
    var cur = cm.getCursor();
    return cm.getRange(CodeMirror.Pos(cur.line, cur.ch - 1), cur) == "<";
  });
}

function completeIfInTag(cm) {
  return completeAfter(cm, function () {
    var tok = cm.getTokenAt(cm.getCursor());
    if (tok.type == "string" && (!/['"]/.test(tok.string.charAt(tok.string.length - 1)) || tok.string.length == 1)) return false;
    var inner = CodeMirror.innerMode(cm.getMode(), tok.state).state;
    return inner.tagName;
  });
}


const cmOptions = ref({
  mode: "application/xml", // Language mode
  theme: "mdn-like", // Theme
  tabSize: 4,
  line: true,
  lineNumbers: true, // Show line number
  smartIndent: true, // Smart indent
  indentUnit: 2, // The smart indent unit is 2 spaces in length
  foldGutter: true, // Code folding
  lineWrapping: false, //宽度超过是否换行
  gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
  highlightSelectionMatches: {showToken: /\w/, annotateScrollbar: true},
  styleActiveLine: true, // Display the style of the selected row
  styleSelectedText: true,
  matchBrackets: true, // 括号匹配.
  showCursorWhenSelecting: true,
  autoCloseTags: true,
  scrollbarStyle: 'simple',
  extraKeys: {
    "'<'": completeAfter,
    "'/'": completeIfAfterLt,
    "' '": completeIfInTag,
    "'='": completeIfInTag,
    "Ctrl-Space": "autocomplete"
  },
})


//测试
let code1 = ref(`<template>
  <div class="example">
    <toolbar
      :config="config"
      :disabled="loading"
      :themes="Object.keys(themes)"
      :languages="Object.keys(languages)"
      @language="ensureLanguageCode"
    />
    <div class="divider"></div>
    <div class="loading-box" v-if="loading">
      <loading />
    </div>
    <editor
      v-else-if="currentLangCode"
      :config="config"
      :theme="currentTheme"
      :language="currentLangCode.language"
      :code="currentLangCode.code"
    />
  </div>
</template>`)
</script>
<style>
.cm-matchhighlight {
  background: lightgreen !important
}

</style>
