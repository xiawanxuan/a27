import { get, post } from '../utils/request';
import { Taxonomy } from '../types';

export const getTaxonomyTree = () => {
  return get<Taxonomy[]>('/taxonomy/tree');
};

export const getTaxonomyChildren = (id: number) => {
  return get<Taxonomy[]>(`/taxonomy/${id}/children`);
};

export const getTaxonomyDetail = (id: number) => {
  return get<Taxonomy>(`/taxonomy/${id}`);
};

export const getTaxonomyByRank = (rank: string) => {
  return get<Taxonomy[]>(`/taxonomy/rank/${rank}`);
};

export const searchTaxonomy = (name: string) => {
  return get<Taxonomy[]>('/taxonomy/search', { name });
};

export const createTaxonomy = (data: Partial<Taxonomy>) => {
  return post<Taxonomy>('/taxonomy', data);
};

export const updateTaxonomy = (id: number, data: Partial<Taxonomy>) => {
  return post<Taxonomy>(`/taxonomy/${id}`, data);
};

export const deleteTaxonomy = (id: number) => {
  return post<void>(`/taxonomy/${id}`);
};

export const getTaxonomyPath = (id: number) => {
  return get<Taxonomy[]>(`/taxonomy/${id}/path`);
};
