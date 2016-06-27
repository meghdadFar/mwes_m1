# mwes_m1

Introduction:
=======================================================

The unige.cui.meghdad.nlp.collocation package implements a model of extracting two-word multiword expressions (MWEs) or collocations based on non-substitutability criterion. Non-substitutability means that the components of a MWE can not be replaced with their near synonyms. For instance "swimming pool" can not be rephrased as "swimming pond" although the latter is a semantically and syntactically plausible alternative. Efficient extraction of MWEs can improve the performance of several other NLP tasks such as IE, parsing, topic models and sentiment analysis. 

For more information about non-substitutability see: 

- Manning, Chris, and Hinrich Schütze. "Collocations." Foundations of statistical natural language processing (1999): 141-77.; 

- Pearce, Darren. "Synonymy in collocation extraction." Proceedings of the workshop on WordNet and other lexical resources, second meeting of the NAACL. 2001). 

unige.cui.meghdad.nlp.collocation with some modifications implements the model presented at: Farahmand, Meghdad, and Joakim Nivre. "Modeling the Statistical Idiosyncrasy of Multiword Expressions." Proceedings of NAACL-HLT. 2015. (ONLY BIDIRECTIONAL MODEL AVAILABLE IN THIS RELEASE).


COMMAND LINE QUICK START:
=======================================================

The program has two modes: evaluation and extraction. Extraction is used to extract a set of collocations from corpus; evaluation is used to evaluate the performance of the model on a labeled dataset in terms of precision, recall and F1 score.
 
For extraction (-evalMode 0) only the following flag is required to be set: -p2corpus (path to corpus). See below for other available options. In command line change directory to the directory of cui-mf-nlp.jar and execute the following command: 

java -cp cui-mf-nlp.jar unige.cui.meghdad.nlp.collocation.RunCollocBidirect_v3 -p2corpus path-to-corpus

-------------------------------------------------------

For evaluation (-evalMode 1) -p2e, -p2lex, -p2bigr, -labels, -maxAlpha, -minAlpha, -step must be set. See below for description of these and other available options. Change directory to the directory of cui-mf-nlp.jar and execute the following command:

java -cp cui-mf-nlp.jar unige.cui.meghdad.nlp.collocation.RunCollocBidirect_v3 -p2corpus path-to-corpus -evalMode 1 -p2e path_2_examples -labels path_2_labels -maxAlpha 10 -minAlpha 2 -step 2


Options:
=======================================================

-p2corpus: path to corpus (must be POS tagged).
-evalMode: evaluation (1) or extraction (0) mode (default value = 0).

-p2e: path to examples (must be POS tagged). This flag must be set in evaluation mode. 

-alpha: alpha parameter in the following inequalities: P(W2|W1) > alpha*P(W2|Syn(W1)) and P(W1|W2) > alpha*P(W1|Syn(W2)). Available only in extraction mode (default value = 2).

If evalMode is set to 1, the following should be set:

-labels: path to a file containing class labels (one label per line). True collocation label is 1, and non-collocation label is 0. 
-maxAlpha: maximum value of alpha
-minAlpha: minimum value of alpha
-step: program alternates alpha (between maximum and minimum values) at this rate.

other options:

-wordTh: Threshold on frequency of extracted words (required by the model). Default value = 3.
-bigramTh: Threshold on frequency of extracted bigrams (required by the model). Default value = 3.
-mwePattern: MWE pattern. This release supports JJ NN (jj-nn) and NN NN (nn-nn) pairs. Default value = nn-nn.
-ignoreCase: Whether (1) or not (0) ignore case (default value = 1).


SampleFiles:
=======================================================

- wiki.pos.sample.txt: A sample of POS Tagged 2015 English Wikipedia
- examples.pos.csv: Set of labeled examples extracted from Wikipedia. 
- labels.csv: class Labels of the examples based on the judgments of four experts.

Labeled data from: Meghdad Farahmand, Aaron Smith, and Joakim Nivre. A Multiword Expression Data Set: Annotating Non-Compositionality and Conventionalization for English Noun Compounds, In Proceedings of the 11th Workshop on Multiword Expressions, North American Chapter of ACL (MWE-NAACL 2015). Denver, USA, June 2015.


Notes:
=======================================================

- Hyphenated terms, and bigrams that contain one or more hyphenated terms are ignored. To consider such bigrams and terms you have to change the patterns for recognising bigrams, unigrams and candidates inside the source codes. 
E.g. 
from
"^(\\w+)\\s(\\w+)$" 
to 
"^(\\w+-?\\w+)\\s(\\w+-?\\w+)$"


Changes:
=======================================================

v2	Directly accepts POS tagged corpus as input. New options: mwe pattern. ignore case. 


Contact:
=======================================================

To report bugs and other issues and if you have any question please contact: meghdad.farahmand@unige.ch


