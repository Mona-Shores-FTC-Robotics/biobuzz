{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-exit-west-1",
      "color": "#3cc8e4",
      "name": "START to EXIT_WEST",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 28,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-north-shot-2",
      "color": "#3cc8e4",
      "name": "EXIT_WEST to NORTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [
        {
          "x": 18,
          "y": 62
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 301
      }
    },
    {
      "id": "to-pick-in-3",
      "color": "#3cc8e4",
      "name": "NORTH_SHOT to PICK_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 12,
        "y": 96
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 301,
        "endDeg": 90
      }
    },
    {
      "id": "to-partner-pick-4",
      "color": "#3cc8e4",
      "name": "PICK_IN to PARTNER_PICK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 12,
        "y": 111
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-north-shot-5",
      "color": "#3cc8e4",
      "name": "PARTNER_PICK to NORTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 301
      }
    },
    {
      "id": "to-garden-6",
      "color": "#3cc8e4",
      "name": "NORTH_SHOT to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 92
        },
        {
          "x": 14,
          "y": 60
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 301,
        "endDeg": 270
      }
    },
    {
      "id": "to-south-shot-7",
      "color": "#3cc8e4",
      "name": "GARDEN to SOUTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 49
      }
    },
    {
      "id": "to-spill-in-8",
      "color": "#3cc8e4",
      "name": "SOUTH_SHOT to SPILL_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29,
        "y": 21
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 49,
        "endDeg": 0
      }
    },
    {
      "id": "to-spill-end-9",
      "color": "#3cc8e4",
      "name": "SPILL_IN to SPILL_END",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 21
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 0
      }
    },
    {
      "id": "to-south-shot-10",
      "color": "#3cc8e4",
      "name": "SPILL_END to SOUTH_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 0,
        "endDeg": 49
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-exit-west-1"
    },
    {
      "kind": "path",
      "lineId": "to-north-shot-2"
    },
    {
      "kind": "path",
      "lineId": "to-pick-in-3"
    },
    {
      "kind": "path",
      "lineId": "to-partner-pick-4"
    },
    {
      "kind": "path",
      "lineId": "to-north-shot-5"
    },
    {
      "kind": "path",
      "lineId": "to-garden-6"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-7"
    },
    {
      "kind": "path",
      "lineId": "to-spill-in-8"
    },
    {
      "kind": "path",
      "lineId": "to-spill-end-9"
    },
    {
      "kind": "path",
      "lineId": "to-south-shot-10"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 60,
    "maxAcceleration": 55,
    "maxDeceleration": 55,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "partner-three-tip",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll"
      ],
      "conditions": [
        "Tip",
        "IntakeFull"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "NORTH_SHOT": [
        40,
        116,
        301
      ],
      "SPILL_IN": [
        29,
        21,
        0
      ],
      "SPILL_END": [
        60,
        21,
        0
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "SOUTH_SHOT": [
        36,
        30,
        49
      ],
      "EXIT_WEST": [
        28,
        10,
        90
      ],
      "PARTNER_PICK": [
        12,
        111,
        90
      ],
      "PICK_IN": [
        12,
        96,
        90
      ]
    },
    "pathEnds": {
      "to-exit-west-1": "EXIT_WEST",
      "to-north-shot-2": "NORTH_SHOT",
      "to-pick-in-3": "PICK_IN",
      "to-partner-pick-4": "PARTNER_PICK",
      "to-north-shot-5": "NORTH_SHOT",
      "to-garden-6": "GARDEN",
      "to-south-shot-7": "SOUTH_SHOT",
      "to-spill-in-8": "SPILL_IN",
      "to-spill-end-9": "SPILL_END",
      "to-south-shot-10": "SOUTH_SHOT"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-2",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-3",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "d-6",
        "kind": "firstOf",
        "label": "Tip 1?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "label": "Yes",
            "cards": []
          },
          {
            "afterMs": 2000,
            "label": "No",
            "cards": [
              {
                "id": "a-4",
                "kind": "action",
                "name": "LaunchOne"
              },
              {
                "id": "w-5",
                "kind": "firstOf",
                "label": "Tip 1 (4th POLLEN)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1500,
                    "cards": []
                  }
                ]
              }
            ]
          }
        ]
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Spill rolls in",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1800,
            "cards": []
          }
        ]
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-exit-west-1",
        "park": false
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-north-shot-2",
        "park": false
      },
      {
        "id": "a-10",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-pick-in-3",
        "park": false
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-partner-pick-4",
        "park": false
      },
      {
        "id": "w-13",
        "kind": "firstOf",
        "label": "Partner's POLLEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 800,
            "cards": []
          }
        ]
      },
      {
        "id": "p-14",
        "kind": "path",
        "lineId": "to-north-shot-5",
        "park": false
      },
      {
        "id": "w-15",
        "kind": "firstOf",
        "label": "Tip 2",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-16",
        "kind": "path",
        "lineId": "to-garden-6",
        "park": false
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "Collect in the GARDEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 800,
            "cards": []
          }
        ]
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-south-shot-7",
        "park": false
      },
      {
        "id": "a-19",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "p-20",
        "kind": "path",
        "lineId": "to-spill-in-8",
        "park": false
      },
      {
        "id": "p-21",
        "kind": "path",
        "lineId": "to-spill-end-9",
        "park": false
      },
      {
        "id": "w-22",
        "kind": "firstOf",
        "label": "Rest of the spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 300,
            "cards": []
          }
        ]
      },
      {
        "id": "p-23",
        "kind": "path",
        "lineId": "to-south-shot-10",
        "park": false
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "Tip 3",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      }
    ]
  },
  "version": "1.5.0"
}