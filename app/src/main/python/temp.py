import numpy as np
from scipy import signal

from google.cloud import aiplatform
from typing import Dict, List, Union
# from google.cloud import aiplatform
from google.protobuf import json_format
from google.protobuf.struct_pb2 import Value

import os
from os.path import dirname, join
json_file = join(dirname(__file__), "stable-sign-388019-c7da022c0572.json")
pcm_file = join(dirname(__file__), "recording_temp.pcm")
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = json_file
# os.environ['GRPC_DNS_RESOLVER'] = 'native'


from typing import Dict, List, Union
from google.cloud import aiplatform
from google.protobuf import json_format
from google.protobuf.struct_pb2 import Value


def predict_custom_trained_model_sample(
    project: str,
    endpoint_id: str,
    instances: Union[Dict, List[Dict]],
    location: str = "us-central1",
    api_endpoint: str = "us-central1-aiplatform.googleapis.com",
):
    """
    `instances` can be either single instance of type dict or a list
    of instances.
    """
    # The AI Platform services require regional API endpoints.
    client_options = {"api_endpoint": api_endpoint}
    # Initialize client that will be used to create and send requests.
    # This client only needs to be created once, and can be reused for multiple requests.
    client = aiplatform.gapic.PredictionServiceClient(client_options=client_options)
    # The format of each instance should conform to the deployed model's prediction input schema.
    instances = instances if type(instances) == list else [instances]
    instances = [
        json_format.ParseDict(instance_dict, Value()) for instance_dict in instances
    ]
    parameters_dict = {}
    parameters = json_format.ParseDict(parameters_dict, Value())
    endpoint = client.endpoint_path(
        project=project, location=location, endpoint=endpoint_id
    )
    response = client.predict(
        endpoint=endpoint, instances=instances, parameters=parameters
    )
    # print("response")
    # print(" deployed_model_id:", response.deployed_model_id)
    # The predictions are a google.protobuf.Value representation of the model's predictions.
    return response.predictions

# stable-sign-388019-c7da022c0572


class highpassfilter():
    def __init__(self):
        self.frequency = 63333

    def butter_highpass_lowpass(self, cutoff, fs, order=5):
        nyq = 0.5 * fs
        normal_cutoff = cutoff / nyq
        high = 10000 / nyq
        b, a = signal.butter(order, normal_cutoff, btype='high', analog=False)
        c, d = signal.butter(order, high, btype='low', analog=False)
        return b, a, c, d

    def butter_highpass_filter(self, data, cutoff, fs, order=5):
        b, a, c, d = self.butter_highpass_lowpass(cutoff, fs, order=order)
        y = signal.filtfilt(b, a, data)
        y = signal.filtfilt(c, d, y)
        return y

def find_index(pcm_data):
    index_f = 0
    amplitude = 10000
    while index_f > 16000 or index_f < 8000:
        index_f = np.argmax(pcm_data > amplitude)
        amplitude += 100
        if amplitude > 30000:
            break
    max = np.max(pcm_data[index_f:index_f + 3000])
    if np.max(pcm_data) != max:
        print(np.max(pcm_data) )
    indices = np.where(pcm_data > max - 5000)[0]
    indices = indices[indices < index_f + 3000]
    id = np.max(indices)
#     waveform = torch.from_numpy(pcm_data.copy()[id:id + 3000]).float()
    freq, t, stft = signal.spectrogram(pcm_data[id:id + 3000], fs=63333, mode='magnitude', nperseg=10, noverlap=1, nfft = 1000)
    return stft.T.flatten()

def temp(data):
    data = np.frombuffer(data, dtype=np.int16)
    hi = highpassfilter()
    output = hi.butter_highpass_filter(data, 2000, 63333)
    output = find_index(output)
    return output


def cloud_infer(data):
    with open(pcm_file, 'rb') as f:
        pcm_data = np.frombuffer(f.read(), dtype=np.int16).tolist()
#     pcm_data = np.random.rand(50000)
    preds = predict_custom_trained_model_sample(
        project="635622715090",
        endpoint_id="2616340694851125248",
        location="us-central1",
        instances={ "pcm": pcm_data }
    )
    print(preds)
    return 1
