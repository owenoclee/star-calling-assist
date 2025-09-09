package com.starcallingassist.modules.sidepanel;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.starcallingassist.StarCallingAssistConfig;
import com.starcallingassist.constants.PluginColors;
import com.starcallingassist.enums.Region;
import com.starcallingassist.events.RouteViaShortestPathRequested;
import com.starcallingassist.events.ShowWorldPointOnWorldMapRequested;
import com.starcallingassist.events.WorldHopRequest;
import com.starcallingassist.modules.sidepanel.decorators.HeaderPanelDecorator;
import com.starcallingassist.modules.sidepanel.decorators.MasterPanelDecorator;
import com.starcallingassist.modules.sidepanel.decorators.StarListGroupEntryDecorator;
import com.starcallingassist.modules.sidepanel.enums.OrderBy;
import com.starcallingassist.modules.sidepanel.enums.TotalLevelType;
import com.starcallingassist.modules.sidepanel.panels.HeaderPanel;
import com.starcallingassist.modules.sidepanel.panels.StarListPanel;
import com.starcallingassist.objects.Star;
import com.starcallingassist.objects.StarLocation;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Setter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.Activatable;
import net.runelite.client.ui.PluginPanel;
import net.runelite.http.api.worlds.World;

public class SidePanel extends PluginPanel implements Activatable
{
	@Setter
	protected Injector injector;

	@Inject
	private StarCallingAssistConfig config;

	@Inject
	private ConfigManager configManager;

	private final MasterPanelDecorator decorator;

	@Setter
	private int currentWorld = 0;

	private final HeaderPanel headerPanel;

	private final StarListPanel starListPanel;

	public SidePanel(MasterPanelDecorator decorator)
	{
		super(false);
		this.decorator = decorator;
		setLayout(new BorderLayout());
		setBackground(PluginColors.SCROLLBOX_BACKGROUND);

		starListPanel = new StarListPanel(new StarListGroupEntryDecorator()
		{

			@Override
			public boolean hasAuthorization()
			{
				return SidePanel.this.hasAuthorization();
			}

			@Override
			public boolean shouldEstimateTier()
			{
				return config.estimateTier();
			}

			@Override
			public boolean showFreeToPlayWorlds()
			{
				return config.showF2P();
			}

			@Override
			public boolean showMembersWorlds()
			{
				return config.showMembers();
			}

			@Override
			public boolean showPvPWorlds()
			{
				return config.showPvp();
			}

			@Override
			public boolean showHighRiskWorlds()
			{
				return config.showHighRisk();
			}

			@Override
			public TotalLevelType maxTotalLevel()
			{
				return config.totalLevelType();
			}

			@Override
			public int minTier()
			{
				return config.minTier();
			}

			@Override
			public int maxTier()
			{
				return config.maxTier();
			}

			@Override
			public int minDeadTime()
			{
				return config.minDeadTime();
			}

			@Override
			public List<Region> visibleRegions()
			{
				return Arrays.stream(Region.values())
					.filter(region -> Boolean.parseBoolean(configManager.getConfiguration("starcallingassistplugin", region.getKeyName())))
					.collect(Collectors.toList());
			}

			@Override
			public Boolean showWorldTypeColumn()
			{
				return config.showWorldType();
			}

			@Override
			public Boolean showTierColumn()
			{
				return config.showTier();
			}

			@Override
			public Boolean showDeadTimeColumn()
			{
				return config.showDeadTime();
			}

			@Override
			public Boolean showFoundByColumn()
			{
				return config.showFoundBy();
			}

			@Override
			public List<StarLocation> getCurrentPlayerLocations()
			{
				return decorator.getCurrentPlayerRegions();
			}

			@Override
			public int getCurrentWorldId()
			{
				return currentWorld;
			}

			@Override
			public void onWorldHopRequest(WorldHopRequest request)
			{
				decorator.onWorldHopRequest(request);
			}

			@Override
			public void onShowWorldPointOnWorldMapRequested(ShowWorldPointOnWorldMapRequested showWorldPointOnWorldMapRequested)
			{
				decorator.onShowWorldPointOnWorldMapRequested(showWorldPointOnWorldMapRequested);
			}

			@Override
			public void onRouteViaShortestPathRequested(RouteViaShortestPathRequested routeViaShortestPathRequested)
			{
				decorator.onRouteViaShortestPathRequested(routeViaShortestPathRequested);
			}
		});

		headerPanel = new HeaderPanel(new HeaderPanelDecorator()
		{
			@Override
			public boolean hasAuthorization()
			{
				return SidePanel.this.hasAuthorization();
			}

			@Override
			public OrderBy getOrderBy()
			{
				return config.orderBy();
			}

			@Override
			public void onSortingChanged(OrderBy orderBy)
			{
				starListPanel.setOrderByColumn(orderBy);
				starListPanel.rebuild();
			}
		});

		add(headerPanel, BorderLayout.NORTH);
		add(starListPanel, BorderLayout.CENTER);
	}

	private boolean hasAuthorization()
	{
		return !config.getAuthorization().isEmpty();
	}

	public void startUp()
	{
		headerPanel.startUp();
		starListPanel.startUp();
	}

	public void shutDown()
	{
		//
	}

	public void onStarUpdate(@Nonnull Star star, @Nonnull World world, long updatedAt)
	{
		starListPanel.onStarUpdate(star, world, updatedAt);
	}

	public void setErrorMessage(String errorMessage)
	{
		headerPanel.setErrorMessage(errorMessage);
		rebuild();
	}

	public void rebuild()
	{
		headerPanel.rebuild();
		starListPanel.rebuild();

		revalidate();
		repaint();
	}

	@Override
	public void onActivate()
	{
		decorator.onSidePanelVisibilityChanged(true);
	}

	@Override
	public void onDeactivate()
	{
		decorator.onSidePanelVisibilityChanged(false);
	}
}