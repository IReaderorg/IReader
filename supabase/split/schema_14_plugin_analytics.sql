-- Schema 14: Plugin Analytics
-- Tables for usage stats, crash reports, and A/B testing

-- Plugin usage statistics table
CREATE TABLE IF NOT EXISTS plugin_usage_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL UNIQUE,
    total_installs BIGINT NOT NULL DEFAULT 0,
    active_installs BIGINT NOT NULL DEFAULT 0,
    total_uninstalls BIGINT NOT NULL DEFAULT 0,
    daily_active_users INTEGER NOT NULL DEFAULT 0,
    weekly_active_users INTEGER NOT NULL DEFAULT 0,
    monthly_active_users INTEGER NOT NULL DEFAULT 0,
    average_session_duration BIGINT NOT NULL DEFAULT 0,
    total_sessions BIGINT NOT NULL DEFAULT 0,
    retention_rate DECIMAL(5, 4) NOT NULL DEFAULT 0,
    crash_free_rate DECIMAL(5, 4) NOT NULL DEFAULT 1,
    average_rating DECIMAL(3, 2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_usage_stats_plugin ON plugin_usage_stats(plugin_id);

-- Plugin analytics events table
CREATE TABLE IF NOT EXISTS plugin_analytics_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    session_id TEXT NOT NULL,
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    properties JSONB NOT NULL DEFAULT '{}',
    metrics JSONB NOT NULL DEFAULT '{}',
    device_info JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analytics_events_plugin ON plugin_analytics_events(plugin_id);
CREATE INDEX IF NOT EXISTS idx_analytics_events_type ON plugin_analytics_events(event_type);
CREATE INDEX IF NOT EXISTS idx_analytics_events_timestamp ON plugin_analytics_events(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_analytics_events_user ON plugin_analytics_events(user_id);

-- Partition by month for better performance
-- CREATE TABLE plugin_analytics_events_y2024m01 PARTITION OF plugin_analytics_events
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Crash reports table
CREATE TABLE IF NOT EXISTS plugin_crash_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    plugin_version TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    error_type TEXT NOT NULL,
    error_message TEXT NOT NULL,
    stack_trace TEXT NOT NULL,
    device_info JSONB NOT NULL,
    breadcrumbs JSONB NOT NULL DEFAULT '[]',
    custom_data JSONB NOT NULL DEFAULT '{}',
    occurrence_count INTEGER NOT NULL DEFAULT 1,
    affected_users INTEGER NOT NULL DEFAULT 1,
    status TEXT NOT NULL DEFAULT 'NEW',
    assigned_to UUID REFERENCES auth.users(id),
    resolved_in_version TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_crash_reports_plugin ON plugin_crash_reports(plugin_id);
CREATE INDEX IF NOT EXISTS idx_crash_reports_status ON plugin_crash_reports(status);
CREATE INDEX IF NOT EXISTS idx_crash_reports_timestamp ON plugin_crash_reports(timestamp DESC);

-- Crash groups table (aggregated crashes)
CREATE TABLE IF NOT EXISTS plugin_crash_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    error_type TEXT NOT NULL,
    error_message TEXT NOT NULL,
    first_occurrence TIMESTAMPTZ NOT NULL,
    last_occurrence TIMESTAMPTZ NOT NULL,
    occurrence_count INTEGER NOT NULL DEFAULT 1,
    affected_users INTEGER NOT NULL DEFAULT 1,
    affected_versions TEXT[] NOT NULL DEFAULT '{}',
    status TEXT NOT NULL DEFAULT 'NEW',
    sample_crash_id UUID REFERENCES plugin_crash_reports(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_crash_groups_plugin ON plugin_crash_groups(plugin_id);
CREATE INDEX IF NOT EXISTS idx_crash_groups_status ON plugin_crash_groups(status);

-- A/B tests table
CREATE TABLE IF NOT EXISTS plugin_ab_tests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    variants JSONB NOT NULL,
    target_audience JSONB NOT NULL DEFAULT '{}',
    start_date TIMESTAMPTZ NOT NULL,
    end_date TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'DRAFT',
    primary_metric TEXT NOT NULL,
    secondary_metrics TEXT[] NOT NULL DEFAULT '{}',
    minimum_sample_size INTEGER NOT NULL DEFAULT 1000,
    confidence_level DECIMAL(3, 2) NOT NULL DEFAULT 0.95,
    created_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ab_tests_plugin ON plugin_ab_tests(plugin_id);
CREATE INDEX IF NOT EXISTS idx_ab_tests_status ON plugin_ab_tests(status);

-- A/B test assignments table
CREATE TABLE IF NOT EXISTS plugin_ab_test_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id UUID NOT NULL REFERENCES plugin_ab_tests(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    variant_id TEXT NOT NULL,
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(test_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_ab_assignments_test ON plugin_ab_test_assignments(test_id);
CREATE INDEX IF NOT EXISTS idx_ab_assignments_user ON plugin_ab_test_assignments(user_id);

-- A/B test conversions table
CREATE TABLE IF NOT EXISTS plugin_ab_test_conversions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id UUID NOT NULL REFERENCES plugin_ab_tests(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    variant_id TEXT NOT NULL,
    conversion_value DECIMAL(10, 4),
    converted_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ab_conversions_test ON plugin_ab_test_conversions(test_id);

-- Performance metrics table
CREATE TABLE IF NOT EXISTS plugin_performance_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_id TEXT NOT NULL,
    metric_name TEXT NOT NULL,
    value DECIMAL(20, 6) NOT NULL,
    unit TEXT NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    percentile INTEGER,
    tags JSONB NOT NULL DEFAULT '{}'
);

CREATE INDEX IF NOT EXISTS idx_perf_metrics_plugin ON plugin_performance_metrics(plugin_id);
CREATE INDEX IF NOT EXISTS idx_perf_metrics_name ON plugin_performance_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_perf_metrics_timestamp ON plugin_performance_metrics(timestamp DESC);

-- RLS Policies
ALTER TABLE plugin_usage_stats ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_analytics_events ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_crash_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_crash_groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_ab_tests ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_ab_test_assignments ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_ab_test_conversions ENABLE ROW LEVEL SECURITY;
ALTER TABLE plugin_performance_metrics ENABLE ROW LEVEL SECURITY;

-- Developers can view their plugin stats
CREATE POLICY "Developers can view own plugin stats" ON plugin_usage_stats
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM developer_plugins dp
            WHERE dp.plugin_id = plugin_usage_stats.plugin_id
            AND dp.developer_id = auth.uid()
        )
    );

-- Developers can view their plugin analytics
CREATE POLICY "Developers can view own analytics" ON plugin_analytics_events
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM developer_plugins dp
            WHERE dp.plugin_id = plugin_analytics_events.plugin_id
            AND dp.developer_id = auth.uid()
        )
    );

-- Developers can view their crash reports
CREATE POLICY "Developers can view own crash reports" ON plugin_crash_reports
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM developer_plugins dp
            WHERE dp.plugin_id = plugin_crash_reports.plugin_id
            AND dp.developer_id = auth.uid()
        )
    );

-- Developers can manage their A/B tests
CREATE POLICY "Developers can manage own AB tests" ON plugin_ab_tests
    FOR ALL USING (created_by = auth.uid());

-- Functions

-- Function to track analytics event
CREATE OR REPLACE FUNCTION track_plugin_event(
    p_plugin_id TEXT,
    p_event_type TEXT,
    p_session_id TEXT,
    p_properties JSONB DEFAULT '{}',
    p_metrics JSONB DEFAULT '{}',
    p_device_info JSONB DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    v_event_id UUID;
BEGIN
    INSERT INTO plugin_analytics_events (
        plugin_id, event_type, session_id, user_id,
        properties, metrics, device_info
    )
    VALUES (
        p_plugin_id, p_event_type, p_session_id, auth.uid(),
        p_properties, p_metrics, p_device_info
    )
    RETURNING id INTO v_event_id;
    
    RETURN v_event_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get A/B test variant for user
CREATE OR REPLACE FUNCTION get_ab_test_variant(
    p_test_id UUID,
    p_user_id UUID
)
RETURNS TEXT AS $$
DECLARE
    v_variant_id TEXT;
    v_test RECORD;
    v_random DECIMAL;
    v_cumulative DECIMAL := 0;
    v_variant RECORD;
BEGIN
    -- Check existing assignment
    SELECT variant_id INTO v_variant_id
    FROM plugin_ab_test_assignments
    WHERE test_id = p_test_id AND user_id = p_user_id;
    
    IF v_variant_id IS NOT NULL THEN
        RETURN v_variant_id;
    END IF;
    
    -- Get test details
    SELECT * INTO v_test
    FROM plugin_ab_tests
    WHERE id = p_test_id AND status = 'RUNNING';
    
    IF v_test IS NULL THEN
        RETURN NULL;
    END IF;
    
    -- Assign variant based on weights
    v_random := random();
    
    FOR v_variant IN SELECT * FROM jsonb_array_elements(v_test.variants)
    LOOP
        v_cumulative := v_cumulative + (v_variant.value->>'weight')::DECIMAL;
        IF v_random <= v_cumulative THEN
            v_variant_id := v_variant.value->>'id';
            EXIT;
        END IF;
    END LOOP;
    
    -- Save assignment
    INSERT INTO plugin_ab_test_assignments (test_id, user_id, variant_id)
    VALUES (p_test_id, p_user_id, v_variant_id);
    
    RETURN v_variant_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to calculate A/B test results
CREATE OR REPLACE FUNCTION calculate_ab_test_results(p_test_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_results JSONB := '[]';
    v_variant RECORD;
    v_sample_size INTEGER;
    v_conversions INTEGER;
    v_conversion_rate DECIMAL;
    v_avg_value DECIMAL;
BEGIN
    FOR v_variant IN 
        SELECT DISTINCT variant_id 
        FROM plugin_ab_test_assignments 
        WHERE test_id = p_test_id
    LOOP
        SELECT COUNT(*) INTO v_sample_size
        FROM plugin_ab_test_assignments
        WHERE test_id = p_test_id AND variant_id = v_variant.variant_id;
        
        SELECT COUNT(*), AVG(conversion_value) INTO v_conversions, v_avg_value
        FROM plugin_ab_test_conversions
        WHERE test_id = p_test_id AND variant_id = v_variant.variant_id;
        
        v_conversion_rate := CASE WHEN v_sample_size > 0 
            THEN v_conversions::DECIMAL / v_sample_size 
            ELSE 0 END;
        
        v_results := v_results || jsonb_build_object(
            'variant_id', v_variant.variant_id,
            'sample_size', v_sample_size,
            'conversions', v_conversions,
            'conversion_rate', v_conversion_rate,
            'average_value', COALESCE(v_avg_value, 0)
        );
    END LOOP;
    
    RETURN v_results;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to get developer dashboard data
CREATE OR REPLACE FUNCTION get_developer_dashboard(p_developer_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_result JSONB;
    v_plugins JSONB;
    v_total_downloads BIGINT := 0;
    v_total_active_users INTEGER := 0;
    v_total_revenue DECIMAL := 0;
BEGIN
    -- Get plugin summaries
    SELECT jsonb_agg(plugin_data) INTO v_plugins
    FROM (
        SELECT jsonb_build_object(
            'plugin_id', dp.plugin_id,
            'plugin_name', dp.plugin_name,
            'usage_stats', row_to_json(pus),
            'crash_free_rate', COALESCE(pus.crash_free_rate, 1),
            'rating', COALESCE(pus.average_rating, 0)
        ) as plugin_data
        FROM developer_plugins dp
        LEFT JOIN plugin_usage_stats pus ON dp.plugin_id = pus.plugin_id
        WHERE dp.developer_id = p_developer_id
    ) plugins;
    
    -- Calculate totals
    SELECT 
        COALESCE(SUM(pus.total_installs), 0),
        COALESCE(SUM(pus.daily_active_users), 0)
    INTO v_total_downloads, v_total_active_users
    FROM developer_plugins dp
    LEFT JOIN plugin_usage_stats pus ON dp.plugin_id = pus.plugin_id
    WHERE dp.developer_id = p_developer_id;
    
    v_result := jsonb_build_object(
        'developer_id', p_developer_id,
        'plugins', COALESCE(v_plugins, '[]'),
        'total_downloads', v_total_downloads,
        'total_active_users', v_total_active_users,
        'total_revenue', v_total_revenue,
        'last_updated', NOW()
    );
    
    RETURN v_result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
