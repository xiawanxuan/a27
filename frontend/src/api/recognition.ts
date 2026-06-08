import { get, post } from '../utils/request';
import { RecognitionRecord, PageResult } from '../types';

export interface RecognitionResultDetail {
  recordId: number;
  imageUrl: string;
  predictedName: string;
  predictedLatinName: string;
  confidence: number;
  topPredictions: Array<{
    name: string;
    latinName: string;
    confidence: number;
  }>;
}

export const identifyImage = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return post<RecognitionResultDetail>('/recognition/identify', formData as any, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const getRecognitionHistory = (params: { page?: number; pageSize?: number }) => {
  return get<PageResult<RecognitionRecord>>('/recognition/history', params);
};

export const getRecognitionDetail = (id: number) => {
  return get<RecognitionRecord>(`/recognition/${id}`);
};

export const confirmRecognition = (id: number, confirmedName?: string, specimenId?: number) => {
  return post<RecognitionRecord>(`/recognition/${id}/confirm`, { confirmedName, specimenId });
};

export const getTopPredictions = (id: number) => {
  return get<Array<{ name: string; latinName: string; confidence: number }>>(
    `/recognition/${id}/predictions`
  );
};
