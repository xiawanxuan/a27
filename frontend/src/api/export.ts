import { get, post } from '../utils/request';
import { ExportTask, PageResult } from '../types';

export interface ExportSpecimensParams {
  keyword?: string;
  taxonomyId?: number;
  collector?: string;
  startDate?: string;
  endDate?: string;
  status?: number;
}

export const exportSpecimens = (params: ExportSpecimensParams) => {
  return post<ExportTask>('/export/specimens', params);
};

export const getExportTaskStatus = (taskId: number) => {
  return get<ExportTask>(`/export/${taskId}/status`);
};

export const getExportTaskList = (params: { page?: number; pageSize?: number }) => {
  return get<PageResult<ExportTask>>('/export/tasks', params);
};

export const getExportDownloadUrl = (taskId: number) => {
  return get<{ url: string }>(`/export/${taskId}/download`);
};
