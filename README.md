# mwes_m1

Introduction:
=======================================================

The `unige.cui.meghdad.nlp.mwe1` package implements a model of extracting two-word multiword expressions (MWEs) or collocations based on non-substitutability criterion. Non-substitutability means that the components of a MWE can not be replaced with their near synonyms. For instance "swimming pool" can not be rephrased as "swimming pond" although the latter is a semantically and syntactically plausible alternative. Efficient extraction of MWEs can improve the performance of several other NLP tasks such as IE, parsing, topic models and sentiment analysis. 

For more information about non-substitutability see: 

- Manning, Chris, and Hinrich Schütze. "Collocations." Foundations of statistical natural language processing (1999): 141-77.; 

- Pearce, Darren. "Synonymy in collocation extraction." Proceedings of the workshop on WordNet and other lexical resources, second meeting of the NAACL. 2001). 

`unige.cui.meghdad.nlp.mwe1` with some modifications implements the model presented at: Farahmand, Meghdad, and Joakim Nivre. "Modeling the Statistical Idiosyncrasy of Multiword Expressions." Proceedings of NAACL-HLT. 2015. (ONLY BIDIRECTIONAL MODEL IS AVAILABLE IN THIS RELEASE).

Note
=======================================================
Since MWEs are better defined on a spectrum of idiosyncrasy and not as a binary phenomena, the program generates a ranked list of MWEs. 
The compounds at the top of this list are those that are least non-substitutable and consequently more idiosyncratic or lexically rigid. 
The compounds at the bottom of the list on the other hand are more substitutable and hence less idiosyncratic.


Command Line Quick Start
=======================================================

 
The program can be used in two ways. 

#### 1. To generate a ranked list of MWEs that are directly extracted from corpus. 

Here, path to the POS tagged corpus must be provided through "-p2corpus" option. 
Other flags that are optional include:

`-maxRank` Indicaates the n top ranked MWEs that will be returned. Defaul=200. 

`-rc` Ranking criteria: delta_12, delta_21, or combined. Default = delta_21. 

#### Example:

`java -cp dist/cui-mf-nlp-mwe-m1.jar unige.cui.meghdad.nlp.mwe1.Collocational_Bidirect_Prob_Corpus -p2corpus "PATH_2_POSTAGGED_CORPUS"`


#### 2. To rank a list of MWE candidates that are provided in an input file. 

Here, path to the list of POS tagged two-word candidates (through -p2POSTaggedCandidates), path to a list of all bigrams (through -p2bigrams) and all unigrams (through -p2unigrams) extracted from the corpus must be provided.
Other flags that are optional include:

`-rc` Ranking criteria: delta_12, delta_21, or combined. Default = delta_21.

#### Example:

`java -cp dist/cui-mf-nlp-mwe-m1.jar unige.cui.meghdad.nlp.mwe1.Collocational_Bidirect_Prob_File -p2POSTaggedCandidates "PATH_2_POSTAGGED_CANDIDATES" -p2bigrams "PATH_2_BIGRAMS" -p2unigrams "PATH_2_UNIGRAMS"`

-------------------------------------------------------

For evaluation (-evalMode 1) -p2e, -p2lex, -p2bigr, -labels, -maxAlpha, -minAlpha, -step must be set. See below for description of these and other available options. Change directory to the directory of cui-mf-nlp.jar and execute the following command:

java -cp cui-mf-nlp.jar unige.cui.meghdad.nlp.mwe1.RunCollocBidirect_v3 -p2corpus path-to-corpus -evalMode 1 -p2e path_2_examples -labels path_2_labels -maxAlpha 10 -minAlpha 2 -step 2


Contact:
=======================================================

To report bugs and other issues and if you have any question please contact: meghdad.farahmand@gmail.com


