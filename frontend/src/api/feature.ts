import { get, post } from '../utils/request';
import { FeatureCompareParams } from '../types';

export interface FeatureExtractResult {
  featureId: number;
  specimenId: number;
  leafLength: number;
  leafWidth: number;
  leafArea: number;
  leafPerimeter: number;
  aspectRatio: number;
  leafShape: string;
  leafMargin: string;
  leafApex: string;
  leafBase: string;
  texture: string;
  colorFeatures: Record<string, any>;
  featureVector: number[];
  extractedAt: string;
}

export interface FeatureCompareResult {
  specimenIds: number[];
  featureNames: string[];
  featureValues: Record<number, Record<string, number>>;
  similarSpecimens: string[];
}

export const extractFeature = (specimenId: number) => {
  return post<FeatureExtractResult>(`/feature/extract/${specimenId}`);
};

export const getSpecimenFeature = (specimenId: number) => {
  return get<FeatureExtractResult>(`/feature/${specimenId}`);
};

export const compareFeatures = (params: FeatureCompareParams) => {
  return post<FeatureCompareResult>('/feature/compare', params);
};
