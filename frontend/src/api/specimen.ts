import { get, post, put, del } from '../utils/request';
import { Specimen, SpecimenQueryParams, PageResult } from '../types';

export const getSpecimenList = (params: SpecimenQueryParams) => {
  return get<PageResult<Specimen>>('/specimens', params);
};

export const getSpecimenDetail = (id: number) => {
  return get<Specimen>(`/specimens/${id}`);
};

export const createSpecimen = (data: Partial<Specimen> & { imageUrls?: string[] }) => {
  return post<Specimen>('/specimens', data);
};

export const updateSpecimen = (id: number, data: Partial<Specimen> & { imageUrls?: string[] }) => {
  return put<Specimen>(`/specimens/${id}`, data);
};

export const deleteSpecimen = (id: number) => {
  return del<void>(`/specimens/${id}`);
};

export const uploadSpecimenImage = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return post<{ url: string }>('/specimens/image/upload', formData as any, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

export const getRecentSpecimens = (limit = 10) => {
  return get<Specimen[]>('/specimens/recent', { limit });
};

export const getSpecimenStats = () => {
  return get<number>('/specimens/stats/count');
};
