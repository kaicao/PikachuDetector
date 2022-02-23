# reference https://gist.github.com/wbrickner/efedf8ab0ce1705de1372c1e2f49dd98
import os
import glob
import pandas as pd
import xml.etree.ElementTree as ET


def xml_to_csv(path):
    xml_list = []
    files = glob.glob(path + '/*.xml')
    for xml_file in files:
        tree = ET.parse(xml_file)
        root = tree.getroot()
        for member in root.findall('object'):
            bndbox = member.find('bndbox')
            value = (root.find('filename').text,
                     int(root.find('size')[0].text),
                     int(root.find('size')[1].text),
                     member.find('name').text,
                     int(bndbox.find('xmin').text),
                     int(bndbox.find('ymin').text),
                     int(bndbox.find('xmax').text),
                     int(bndbox.find('ymax').text)
                     )
            xml_list.append(value)
    column_name = ['filename', 'width', 'height', 'class', 'xmin', 'ymin', 'xmax', 'ymax']
    xml_df = pd.DataFrame(xml_list, columns=column_name)
    return xml_df


def main():
    #image_path = os.path.join(os.getcwd(), 'train_pikachu')
    #xml_df = xml_to_csv(image_path)
    #xml_df.to_csv('pikachu_train_labels.csv', index=None)
    #print('Successfully converted train labels xml to csv.')

    image_path = os.path.join(os.getcwd(), 'test_pikachu')
    xml_df = xml_to_csv(image_path)
    xml_df.to_csv('pikachu_test_labels.csv', index=None)
    print('Successfully converted test labels xml to csv.')


main()