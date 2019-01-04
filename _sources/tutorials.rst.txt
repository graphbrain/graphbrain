=========
Tutorials
=========

Some tutorials for common tasks.

Training the hypergenerator
===========================

The hypergenerator is an important step in converting natural language into hyperedges. For now it is necessary to
train the model. The training data can be generated with the help of the dataset under
``datasets/training_data/hyperedge_generator/parses.txt``.

Such parse datasets can be created or extended with the interactive edge builder. For now let's use the provided one
to generate the training data::

   $ graphbrain --infile datasets/training_data/hyperedge_generator/parses.txt --outfile cases.txt generate_hypergen_cases

The above command will create the ``cases.txt`` file on the current directory. We can now use it to train the model::

   $ graphbrain --infile cases.txt learn_hypergen

This will create the ``hypergen_random_forest.model`` file on the current directory, which will be used by default by
the natural language parser.

You can test the precision of this model with::

   $ graphbrain --infile datasets/training_data/hyperedge_generator/parses.txt test_hypergen

Notice that the model is trained with 75% of the dataset and its precison is tested with the remaining 25%.
