package tech.fastj.graphics.game;

import tech.fastj.math.Point;
import tech.fastj.math.Pointf;
import tech.fastj.math.Transform2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Path2D;
import java.util.Objects;

/**
 * A builder class for creating {@link Polygon2D} objects.
 *
 * @author Andrew Dey
 * @since 1.5.0
 */
public class Polygon2DBuilder {

    private final Pointf[] points;
    private final Point[] altIndexes;
    private final boolean shouldRender;

    private RenderStyle renderStyle = Polygon2D.DefaultRenderStyle;
    private Paint fillPaint = Polygon2D.DefaultFill;
    private BasicStroke outlineStroke = Polygon2D.DefaultOutlineStroke;
    private Color outlineColor = Polygon2D.DefaultOutlineColor;

    private Pointf translation = Transform2D.DefaultTranslation.copy();
    private float rotation = Transform2D.DefaultRotation;
    private Pointf scale = Transform2D.DefaultScale.copy();

    /**
     * {@link Polygon2DBuilder} constructor, taking in a set of points, a set of alternate indexes (for defining curves), and whether the
     * polygon should be rendered.
     *
     * @param points       The {@link Pointf} array of mesh points to use for the resulting {@link Polygon2D}.
     * @param altIndexes   The {@link Point} array of alternate indexes defining where curves are in the array of points, as well as other
     *                     {@link Path2D} options.
     * @param shouldRender The "should render" {@link boolean} to use for the resulting {@link Polygon2D}.
     */
    Polygon2DBuilder(Pointf[] points, Point[] altIndexes, boolean shouldRender) {
        this.points = Objects.requireNonNull(points, "The array of points must not be null.");
        this.altIndexes = altIndexes;
        this.shouldRender = shouldRender;
    }

    /**
     * Sets the builder's render style value.
     *
     * @param renderStyle The {@link RenderStyle} to be used in the resulting {@link Polygon2D}.
     * @return The {@link Polygon2DBuilder}, for method chaining.
     */
    public Polygon2DBuilder withRenderStyle(RenderStyle renderStyle) {
        this.renderStyle = Objects.requireNonNull(renderStyle, "The render style must not be null.");
        return this;
    }

    /**
     * Sets the builder's fill paint value.
     *
     * @param fillPaint The fill {@link Paint} to be used in the resulting {@link Polygon2D}.
     * @return The {@link Polygon2DBuilder}, for method chaining.
     */
    public Polygon2DBuilder withFill(Paint fillPaint) {
        this.fillPaint = Objects.requireNonNull(fillPaint, "The fill must not be null.");
        return this;
    }

    /**
     * Sets the builder's outline stroke and outline color values.
     *
     * @param outlineStroke The outline {@link BasicStroke} to be used in the resulting {@link Polygon2D}.
     * @param outlineColor  The outline {@link Color} to be used in the resulting {@link Polygon2D}.
     * @return The {@link Polygon2DBuilder}, for method chaining.
     */
    public Polygon2DBuilder withOutline(BasicStroke outlineStroke, Color outlineColor) {
        this.outlineStroke = Objects.requireNonNull(outlineStroke, "The outline stroke must not be null.");
        this.outlineColor = Objects.requireNonNull(outlineColor, "The outline color must not be null.");
        return this;
    }

    /**
     * Sets the builder's transformation (translation, rotation, scale) values.
     *
     * @param translation The translation {@link Pointf} to be used in the resulting {@link Polygon2D}.
     * @param rotation    The rotation {@link float} to be used in the resulting {@link Polygon2D}.
     * @param scale       The scale {@link Pointf} to be used int he resulting {@link Polygon2D}.
     * @return The {@link Polygon2DBuilder}, for method chaining.
     */
    public Polygon2DBuilder withTransform(Pointf translation, float rotation, Pointf scale) {
        this.translation = Objects.requireNonNull(translation, "The translation value must not be null.");
        this.scale = Objects.requireNonNull(scale, "The scale value must not be null.");
        if (Float.isNaN(rotation)) {
            throw new NumberFormatException("The rotation value must not be NaN.");
        }
        this.rotation = rotation;
        return this;
    }

    /** {@return a new {@link Polygon2D} object, using the data provided by earlier method calls} */
    public Polygon2D build() {
        return (Polygon2D) new Polygon2D(points, altIndexes)
            .setOutlineStroke(outlineStroke)
            .setOutlineColor(outlineColor)
            .setRenderStyle(renderStyle)
            .setFill(fillPaint)
            .setShouldRender(shouldRender)
            .setTransform(translation, rotation, scale);
    }
}
