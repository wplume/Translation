package com.personal.windplume.translation.db;

public class TranslationDBSchema {
    public static final class GlossaryTable {
        public static final String NAME = "glossary";

        public static final class Col {
            public static final String EN_WORD = "en_word";
            public static final String ZH_WORD = "zh_word";
            public static final String EXPLANATION = "explanation";
        }
    }

    public static final class VocabularyTable {
        public static final String NAME = "dictionary";

        public static final class Col{
            public static final String EN_WORD = "en_word";
            public static final String ZH_WORD = "zh_word";
            public static final String EXPLANATION = "explanation";
        }
    }
}
