import numpy as np
from scipy import signal

class HighPassFilter():
    """
    This class is used to filter the low frequency noise in the audio signal.
    """
    def __init__(self):
        self.frequency = 63333

    def butter_highpass(self, cutoff, fs, order=5):
        nyq = 0.5 * fs
        normal_cutoff = cutoff / nyq
        b, a = signal.butter(order, normal_cutoff, btype='high', analog=False)
        return b, a

    def butter_highpass_filter(self, data, cutoff, fs, order=5):
        b, a = self.butter_highpass(cutoff, fs, order=order)
        y = signal.filtfilt(b, a, data)
        return y

def temp(data):
    data = np.frombuffer(data, dtype=np.int16)
    hi = HighPassFilter()
    output = hi.butter_highpass_filter(data, 18000, 63333)
#     a = [1.0, 2.0, 3.002]
    return output
#     c = np.array(a + b)
#     c = c.astype(np.float32)
#     return c.tolist()



# def filter(data):
#
#
#     return output