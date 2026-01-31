'use client';

import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '@/lib/axios';
import { showSuccess, showError, showLoading, dismissToast } from '@/lib/toastHelper';
import { ArrowRightCircle, Loader2, Users, Building2, AlertCircle, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

/** Response from GET /api/admin/promotion/context */
interface PromotionContext {
  canPromote: boolean;
  currentAcademicYear: { id: number; name: string } | null;
  closedAcademicYear: { id: number; name: string } | null;
  message: string | null;
}

const CONTEXT_POLL_INTERVAL_MS = 10000; // refresh context every 10s for real-time feel

export default function PromotionPage() {
  const [classrooms, setClassrooms] = useState([]);
  const [fromClassroomId, setFromClassroomId] = useState('');
  const [toClassroomId, setToClassroomId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [context, setContext] = useState<PromotionContext | null>(null);
  const [contextLoading, setContextLoading] = useState(true);

  const fetchPromotionContext = useCallback(async () => {
    try {
      const res = await apiClient.get<PromotionContext>('/api/admin/promotion/context');
      setContext(res.data ?? null);
    } catch {
      setContext(null);
    } finally {
      setContextLoading(false);
    }
  }, []);

  const fetchClassrooms = useCallback(async () => {
    try {
      const res = await apiClient.get('/api/admin/classrooms');
      setClassrooms(res.data || []);
    } catch {
      showError('Failed to load classrooms.');
    }
  }, []);

  useEffect(() => {
    fetchPromotionContext();
    fetchClassrooms();
  }, [fetchPromotionContext, fetchClassrooms]);

  // Real-time: poll context so admin sees updates after closing/setting year in another tab
  useEffect(() => {
    const interval = setInterval(fetchPromotionContext, CONTEXT_POLL_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [fetchPromotionContext]);

  const handlePromoteClassroom = async () => {
    if (!fromClassroomId || !toClassroomId) {
      showError('Please select both source and target classroom.');
      return;
    }
    if (fromClassroomId === toClassroomId) {
      showError('Source and target classroom must be different.');
      return;
    }
    setIsSubmitting(true);
    const toastId = showLoading('Promoting class…');
    try {
      await apiClient.post('/api/admin/promotion/classroom', {
        fromClassroomId: Number(fromClassroomId),
        toClassroomId: Number(toClassroomId),
      });
      dismissToast(toastId);
      showSuccess('Class promoted successfully.');
      setFromClassroomId('');
      setToClassroomId('');
      fetchPromotionContext(); // refresh context after action
    } catch (err: unknown) {
      dismissToast(toastId);
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        (err as { customMessage?: string })?.customMessage ||
        'Failed to promote class.';
      showError(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const canPromote = context?.canPromote ?? false;
  const guidanceMessage = context?.message ?? null;

  return (
    <div className="space-y-6 p-6 bg-slate-50 min-h-screen">
      <div>
        <h1 className="text-2xl font-bold flex items-center gap-3">
          <ArrowRightCircle className="h-8 w-8 text-blue-600" />
          Promotion
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Promote an entire class from the previous academic year to the current year
        </p>
      </div>

      {/* Real-time promotion context */}
      {!contextLoading && context && (
        <Card className="max-w-xl border-slate-200 bg-white">
          <CardContent className="pt-6">
            <div className="flex items-start gap-3">
              {canPromote ? (
                <CheckCircle2 className="h-5 w-5 text-green-600 shrink-0 mt-0.5" />
              ) : (
                <AlertCircle className="h-5 w-5 text-amber-600 shrink-0 mt-0.5" />
              )}
              <div className="space-y-1">
                {canPromote ? (
                  <>
                    <p className="text-sm font-medium text-slate-700">Ready to promote</p>
                    <p className="text-xs text-slate-500">
                      From: <span className="font-medium">{context.closedAcademicYear?.name ?? '—'}</span>
                      {' → '}
                      To: <span className="font-medium">{context.currentAcademicYear?.name ?? '—'}</span>
                    </p>
                  </>
                ) : (
                  <>
                    <p className="text-sm font-medium text-slate-700">Promotion not available yet</p>
                    <p className="text-sm text-slate-600">{guidanceMessage}</p>
                    {context.closedAcademicYear && (
                      <p className="text-xs text-slate-500">
                        Closed year: {context.closedAcademicYear.name}
                      </p>
                    )}
                    {context.currentAcademicYear && (
                      <p className="text-xs text-slate-500">
                        Current year: {context.currentAcademicYear.name}
                      </p>
                    )}
                  </>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5 text-blue-600" />
            Promote classroom
          </CardTitle>
          <CardDescription>
            Select the source classroom (closed year) and target classroom (current year). All
            active students in the source class will be promoted.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="space-y-2">
            <Label>From classroom (previous year)</Label>
            <Select value={fromClassroomId} onValueChange={setFromClassroomId}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Select source classroom" />
              </SelectTrigger>
              <SelectContent>
                {classrooms.map((c: { id: number; name: string }) => (
                  <SelectItem key={c.id} value={String(c.id)}>
                    <span className="flex items-center gap-2">
                      <Building2 className="h-4 w-4 text-slate-400" />
                      {c.name}
                    </span>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <Label>To classroom (current year)</Label>
            <Select value={toClassroomId} onValueChange={setToClassroomId}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Select target classroom" />
              </SelectTrigger>
              <SelectContent>
                {classrooms.map((c: { id: number; name: string }) => (
                  <SelectItem key={c.id} value={String(c.id)}>
                    <span className="flex items-center gap-2">
                      <Building2 className="h-4 w-4 text-slate-400" />
                      {c.name}
                    </span>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <Button
            onClick={handlePromoteClassroom}
            disabled={
              isSubmitting || !fromClassroomId || !toClassroomId || !canPromote || contextLoading
            }
            className="w-full sm:w-auto"
          >
            {isSubmitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Promote class
          </Button>
        </CardContent>
      </Card>

      <p className="text-xs text-slate-500">
        Ensure the current academic year is set and the previous year is closed in Settings →
        Academic years before promoting. This page updates automatically every 10 seconds.
      </p>
    </div>
  );
}
