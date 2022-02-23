## PikachuDetector
Application performs stream processing through video clips to identify Pikachu within them.

Building model
- Ensure TensorFlow is installed locally, follow reference 1 to install.
- Collect images of Pikachu. E.g. use images from Pikachu datasets project[2]
- Use tool like RectLabel [3] to do object detection of Pikachu among the collected images.
- Separate images to training and testing, and put into separate folder. E.g. dataset module resources/train_pikachu for training, 
resources/test_pikachu for testing.
- Then execute `python3 xml_to_csv.py` [4] to create CSV base on annotated XMLs. 
It's placed in dataset module resources folder, need to adjust `image_path` to correct folder.  
- Next, create TensorFlow TFRecord using `generate_tfrecord.py` [5] in dataset module resources folder. 
Need to provide correct arguments to specify CSV label file (csv_input), source folder (specified in code to raw/pikachu folder) and output file(output_path).   


### Train model
- Clone [TensorFlow Models]() project.
- Install [TensorFlow Object Detection] (https://tensorflow-object-detection-api-tutorial.readthedocs.io/en/latest/install.html)
- Verify by:
```
# From within TensorFlow/models/research/
python object_detection/builders/model_builder_tf2_test.py

# You may need to upgrade opencv
pip install opencv-python --upgrade

python3 object_detection/model_main.py --logtostderr --model_dir=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_mobilenet_v2_training --pipeline_config_path=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_mobilenet_v2_pikachu.config --num_train_steps=2000 --sample_1_of_n_eval_examples=1
python3 object_detection/model_main.py --logtostderr --model_dir=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_training --pipeline_config_path=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_pikachu.config --num_train_steps=2000 --sample_1_of_n_eval_examples=1
```

Execute Tensorboard to see the progress
```
tensorboard --logdir <path to training folder>
``` 

Example result with SSD inception v3 after 2000 rounds
Start 29.12 23 
Finish 30.12 15
2000 steps, loss for final step is 5.444458.

Show saved model: 
```
saved_model_cli show --dir <training folder path> --tag_set serve --signature_def serving_default`
```

 

https://github.com/tensorflow/models/blob/master/research/object_detection/export_inference_graph.py
`tf_example`: Accepts a 1-D string tensor of shape [None] containing
    serialized TFExample protos. Image resolutions are expected to be the same
    if more than 1 image is provided.
```
The given SavedModel SignatureDef contains the following input(s):
  inputs['serialized_example'] tensor_info:
      dtype: DT_STRING
      shape: ()
      name: tf_example:0
The given SavedModel SignatureDef contains the following output(s):
  outputs['detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 100, 4)
      name: detection_boxes:0
  outputs['detection_classes'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 100)
      name: detection_classes:0
  outputs['detection_multiclass_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 100, 38)
      name: detection_multiclass_scores:0
  outputs['detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 100)
      name: detection_scores:0
  outputs['num_detections'] tensor_info:
      dtype: DT_FLOAT
      shape: (1)
      name: num_detections:0
  outputs['raw_detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 5919, 4)
      name: raw_detection_boxes:0
  outputs['raw_detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (1, 5919, 38)
      name: raw_detection_scores:0
Method name is: tensorflow/serving/predict
```

```
python3 object_detection/export_inference_graph.py --input_type image_tensor --pipeline_config_path =/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_training --pipeline_config_path=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_pikachu.config --trained_checkpoint_prefix /Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_training/model.ckpt-2000 --output_directory /Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_image_tensor
```
`image_tensor`: Accepts a uint8 4-D tensor of shape [None, None, None, 3]
Show saved model: 
```
saved_model_cli show --dir <training folder path> --tag_set serve --signature_def serving_default`
The given SavedModel SignatureDef contains the following input(s):
  inputs['inputs'] tensor_info:
      dtype: DT_UINT8
      shape: (-1, -1, -1, 3)
      name: image_tensor:0
The given SavedModel SignatureDef contains the following output(s):
  outputs['detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100, 4)
      name: detection_boxes:0
  outputs['detection_classes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100)
      name: detection_classes:0
  outputs['detection_multiclass_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100, 38)
      name: detection_multiclass_scores:0
  outputs['detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100)
      name: detection_scores:0
  outputs['num_detections'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1)
      name: num_detections:0
  outputs['raw_detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, -1, 4)
      name: raw_detection_boxes:0
  outputs['raw_detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, -1, 38)
      name: raw_detection_scores:0
Method name is: tensorflow/serving/predict

```
```
python3 object_detection/export_inference_graph.py --input_type encoded_image_string_tensor --pipeline_config_path=/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_pikachu.config --trained_checkpoint_prefix /Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_training/model.ckpt-2000 --output_directory /Users/kai/Documents/Projects/MachineLearning/PikachuDetector/dataset/src/main/ssd_inception_v3_encoded_image_input_2
```
`encoded_image_string_tensor`: Accepts a 1-D string tensor of shape [None]
    containing encoded PNG or JPEG images. Image resolutions are expected to be
    the same if more than 1 image is provided.
Show saved model: 
```
saved_model_cli show --dir . --tag_set serve --signature_def serving_default
The given SavedModel SignatureDef contains the following input(s):
  inputs['inputs'] tensor_info:
      dtype: DT_STRING
      shape: (-1)
      name: encoded_image_string_tensor:0
The given SavedModel SignatureDef contains the following output(s):
  outputs['detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100, 4)
      name: detection_boxes:0
  outputs['detection_classes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100)
      name: detection_classes:0
  outputs['detection_multiclass_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100, 38)
      name: detection_multiclass_scores:0
  outputs['detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, 100)
      name: detection_scores:0
  outputs['num_detections'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1)
      name: num_detections:0
  outputs['raw_detection_boxes'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, -1, 4)
      name: raw_detection_boxes:0
  outputs['raw_detection_scores'] tensor_info:
      dtype: DT_FLOAT
      shape: (-1, -1, 38)
      name: raw_detection_scores:0
Method name is: tensorflow/serving/predict

```

### Execute `PikachuDetectJobExecute` Flink job
#### Input arguments
1. Path to the input video file. 
E.g. `/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/flink-processing/src/main/resources/pokemon_detective_pikachu.mp4`
2. Path to the output video file. Folders involved must be existing. 
E.g. `/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/flink-processing/src/main/resources/output.mp4`
3. Path to TensorFlow model file. 
E.g. `/Users/kai/Documents/Projects/MachineLearning/PikachuDetector/flink-processing/src/main/resources/ssd_mobilenet_v2_encoded_image_input/saved_model`

The example video of `pokemon_detective_pikachu.mp4` has:
- time length: 79s
- width: 640
- height: 360
- pixel format: PIX_FMT_YUV420P
- codec: H264
Decoded to 1897 images.
Encode need to wait roughly 42ms in between images.



### Execute flink-processing
Execute `PikachuDetectJobExecutor` main with parameter like
```
<path to input movie folder>/pokemon_detective_pikachu.mp4 <path to movie output folder>/output.mp4 <path to saved model>/ssd_mobilenet_v2_encoded_image_input/saved_model
```

### Note
In case you wanna use Anaconda, currently tensorflow-io is not in its repo, 
thus end up with error when use TensorFlow from there.
To deactivate conda environment when open new terminal use:
```
deactivate
conda config --set auto_activate_base false
```    

### Reference
[1] Install tensorflow locally, follow https://www.tensorflow.org/install/.
```
Docker install
docker pull tensorflow/tensorflow:1.15.0
docker run -it -p 8888:8888 tensorflow/tensorflow:latest-jupyter  # Start Jupyter server
```

[2] [Pikachu datasets](https://github.com/diewland/pika-dataset-v3)
[3] [RectLabel](https://rectlabel.com/)
[4] [xml_to_csv.py](https://gist.github.com/wbrickner/efedf8ab0ce1705de1372c1e2f49dd98)
[5] [generate_tfrecord.py](https://github.com/datitran/raccoon_dataset/blob/master/generate_tfrecord.py)
[6] [Train a Hand Detector using Tensorflow 2 Object Detection API in 2021](https://towardsdatascience.com/train-an-object-detector-using-tensorflow-2-object-detection-api-in-2021-a4fed450d1b9)
[7] [LabelImg](https://github.com/tzutalin/labelImg)