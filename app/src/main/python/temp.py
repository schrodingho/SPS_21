import numpy as np
from scipy import signal

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
